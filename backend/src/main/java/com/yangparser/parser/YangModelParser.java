package com.yangparser.parser;

import com.yangparser.model.YangModule;
import com.yangparser.model.YangNode;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Core YANG file parser.
 * Parses YANG 1.0 / 1.1 files and builds a tree of YangNode objects.
 */
@Component
public class YangModelParser {

    // Node types that can contain children
    private static final Set<String> CONTAINER_TYPES = Set.of(
        "container", "list", "choice", "case", "grouping",
        "augment", "rpc", "input", "output", "notification",
        "anyxml", "uses"
    );

    // Leaf-like node types
    private static final Set<String> LEAF_TYPES = Set.of(
        "leaf", "leaf-list"
    );

    // Module-level keywords to extract as metadata
    private static final Set<String> META_KEYWORDS = Set.of(
        "namespace", "prefix", "organization", "contact",
        "description", "yang-version", "revision"
    );

    public YangModule parse(String yangContent) {
        YangModule module = new YangModule();

        // Clean the content
        String cleaned = preprocessContent(yangContent);

        // Extract module name
        Pattern modulePattern = Pattern.compile("^\\s*module\\s+(\\S+)\\s*\\{", Pattern.MULTILINE);
        Matcher m = modulePattern.matcher(cleaned);
        if (m.find()) {
            module.setModuleName(m.group(1));
        } else {
            module.setModuleName("unknown-module");
        }

        // Extract metadata
        extractMetadata(cleaned, module);

        // Parse the node tree
        List<YangNode> nodes = parseNodes(cleaned, 0);
        module.setNodes(nodes);

        // Build hierarchy text
        StringBuilder hierarchy = new StringBuilder();
        hierarchy.append("module: ").append(module.getModuleName()).append("\n");
        for (int i = 0; i < nodes.size(); i++) {
            appendNodeHierarchy(hierarchy, nodes.get(i), "", i == nodes.size() - 1);
        }
        module.setHierarchyText(hierarchy.toString());

        // Calculate stats
        int[] stats = countNodes(nodes);
        module.setTotalNodes(stats[0]);
        module.setContainerCount(stats[1]);
        module.setLeafCount(stats[2]);
        module.setListCount(stats[3]);

        return module;
    }

    private String preprocessContent(String content) {
        // Remove single-line comments
        content = content.replaceAll("//[^\n]*", "");
        // Remove block comments
        content = content.replaceAll("/\\*[\\s\\S]*?\\*/", "");
        return content;
    }

    private void extractMetadata(String content, YangModule module) {
        module.setYangVersion(extractSimpleValue(content, "yang-version"));
        module.setNamespace(extractSimpleValue(content, "namespace"));
        module.setPrefix(extractSimpleValue(content, "prefix"));
        module.setOrganization(extractBlockValue(content, "organization"));
        module.setContact(extractBlockValue(content, "contact"));
        module.setDescription(extractBlockValue(content, "description"));

        // Extract revision date
        Pattern revPattern = Pattern.compile("revision\\s+([\\d\\-]+)\\s*\\{");
        Matcher rm = revPattern.matcher(content);
        if (rm.find()) {
            module.setRevision(rm.group(1));
        }
    }

    private String extractSimpleValue(String content, String keyword) {
        Pattern p = Pattern.compile(keyword + "\\s+[\"']?([^\"';\\{\\}\\n]+)[\"']?\\s*;");
        Matcher m = p.matcher(content);
        if (m.find()) {
            return m.group(1).trim().replace("\"", "");
        }
        return null;
    }

    private String extractBlockValue(String content, String keyword) {
        // Try quoted string first
        Pattern p = Pattern.compile(keyword + "\\s+\"([^\"]+)\"");
        Matcher m = p.matcher(content);
        if (m.find()) {
            return m.group(1).trim();
        }
        // Try unquoted
        p = Pattern.compile(keyword + "\\s+([^;{]+);");
        m = p.matcher(content);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    /**
     * Parse top-level nodes from the module body.
     * This tokenizes the content into brace-balanced blocks.
     */
    private List<YangNode> parseNodes(String content, int depth) {
        List<YangNode> nodes = new ArrayList<>();

        // Find the module body (content inside the outermost braces)
        int moduleBodyStart = content.indexOf('{');
        if (moduleBodyStart < 0) return nodes;

        String body = extractBlock(content, moduleBodyStart);
        if (body == null) return nodes;

        parseNodeChildren(body, nodes, depth);
        return nodes;
    }

    /**
     * Parse child nodes from a block body string.
     */
    private void parseNodeChildren(String body, List<YangNode> nodes, int depth) {
        int i = 0;
        while (i < body.length()) {
            // Skip whitespace
            while (i < body.length() && Character.isWhitespace(body.charAt(i))) i++;
            if (i >= body.length()) break;

            // Read a keyword
            int wordStart = i;
            while (i < body.length() && !Character.isWhitespace(body.charAt(i))
                    && body.charAt(i) != '{' && body.charAt(i) != '}' && body.charAt(i) != ';') {
                i++;
            }
            if (i == wordStart) { i++; continue; }

            String keyword = body.substring(wordStart, i).trim();

            // Skip whitespace
            while (i < body.length() && Character.isWhitespace(body.charAt(i))) i++;

            // Skip metadata blocks (namespace, prefix, etc. that aren't nodes)
            if (META_KEYWORDS.contains(keyword) || keyword.equals("revision")
                    || keyword.equals("module") || keyword.equals("submodule")
                    || keyword.equals("import") || keyword.equals("include")
                    || keyword.equals("typedef") || keyword.equals("extension")
                    || keyword.equals("feature") || keyword.equals("identity")
                    || keyword.equals("deviation")) {
                // Skip to end of statement or block
                i = skipStatement(body, i);
                continue;
            }

            // Must be a node keyword
            if (!isNodeKeyword(keyword)) {
                i = skipStatement(body, i);
                continue;
            }

            // Read the node name
            int nameStart = i;
            while (i < body.length() && !Character.isWhitespace(body.charAt(i))
                    && body.charAt(i) != '{' && body.charAt(i) != ';') {
                i++;
            }
            String nodeName = body.substring(nameStart, i).trim();
            if (nodeName.isEmpty()) { i = skipStatement(body, i); continue; }

            // Skip whitespace
            while (i < body.length() && Character.isWhitespace(body.charAt(i))) i++;

            YangNode node = new YangNode(nodeName, keyword, depth);

            if (i < body.length() && body.charAt(i) == '{') {
                // Block node - extract body
                String nodeBody = extractBlock(body, i);
                if (nodeBody != null) {
                    i += nodeBody.length() + 2; // skip { body }
                    populateNodeFromBody(node, nodeBody, depth + 1);
                } else {
                    i++;
                }
            } else if (i < body.length() && body.charAt(i) == ';') {
                i++; // simple statement
            } else {
                i = skipStatement(body, i);
            }

            nodes.add(node);
        }
    }

    private boolean isNodeKeyword(String kw) {
        return CONTAINER_TYPES.contains(kw) || LEAF_TYPES.contains(kw)
                || kw.equals("anyxml") || kw.equals("anydata");
    }

    /**
     * Fill in node attributes from its body block.
     */
    private void populateNodeFromBody(YangNode node, String body, int depth) {
        // Extract leaf attributes
        node.setDescription(extractBlockValue(body, "description"));
        node.setMandatory(extractSimpleStatementValue(body, "mandatory"));
        node.setDefaultValue(extractSimpleStatementValue(body, "default"));
        node.setKey(extractSimpleStatementValue(body, "key"));
        node.setMinElements(extractSimpleStatementValue(body, "min-elements"));
        node.setMaxElements(extractSimpleStatementValue(body, "max-elements"));

        // Extract data type
        String typeStr = extractSimpleStatementValue(body, "type");
        if (typeStr != null) {
            node.setDataType(typeStr);
            // Look for range and pattern inside type block
            Pattern typeBlockPattern = Pattern.compile("type\\s+\\S+\\s*\\{([^}]*)\\}");
            Matcher tbm = typeBlockPattern.matcher(body);
            if (tbm.find()) {
                String typeBody = tbm.group(1);
                String range = extractSimpleStatementValue(typeBody, "range");
                String pattern = extractSimpleStatementValue(typeBody, "pattern");
                if (range != null) node.setRange(range);
                if (pattern != null) node.setPattern(pattern);
            }
        }

        // Recursively parse child nodes
        List<YangNode> children = new ArrayList<>();
        parseNodeChildren(body, children, depth);
        node.setChildren(children);
    }

    private String extractSimpleStatementValue(String body, String keyword) {
        Pattern p = Pattern.compile(keyword + "\\s+[\"']?([^\"';\\{\\}\\n]+)[\"']?\\s*;");
        Matcher m = p.matcher(body);
        if (m.find()) {
            return m.group(1).trim().replace("\"", "");
        }
        return null;
    }

    /**
     * Extract the content inside braces starting at index i (which should point to '{').
     */
    private String extractBlock(String content, int start) {
        if (start >= content.length() || content.charAt(start) != '{') return null;
        int depth = 0;
        int begin = start + 1;
        for (int i = start; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return content.substring(begin, i);
                }
            }
        }
        return null;
    }

    /**
     * Skip a statement (to ';' or end of '{...}' block).
     */
    private int skipStatement(String body, int i) {
        int depth = 0;
        while (i < body.length()) {
            char c = body.charAt(i);
            if (c == '{') { depth++; i++; }
            else if (c == '}') {
                if (depth == 0) return i;
                depth--; i++;
            }
            else if (c == ';' && depth == 0) { return i + 1; }
            else i++;
        }
        return i;
    }

    private void appendNodeHierarchy(StringBuilder sb, YangNode node, String prefix, boolean isLast) {
        String connector = isLast ? "└── " : "├── ";
        sb.append(prefix).append(connector);
        sb.append("[").append(node.getType().toUpperCase()).append("] ").append(node.getName());
        if (node.getDataType() != null) sb.append(" : ").append(node.getDataType());
        if ("true".equals(node.getMandatory())) sb.append(" *mandatory*");
        sb.append("\n");

        String childPrefix = prefix + (isLast ? "    " : "│   ");

        // Print constraints
        if (node.getDescription() != null && !node.getDescription().isEmpty()) {
            sb.append(childPrefix).append("    ℹ ").append(node.getDescription()).append("\n");
        }
        if (node.getRange() != null) sb.append(childPrefix).append("    range: ").append(node.getRange()).append("\n");
        if (node.getPattern() != null) sb.append(childPrefix).append("    pattern: ").append(node.getPattern()).append("\n");
        if (node.getKey() != null) sb.append(childPrefix).append("    key: ").append(node.getKey()).append("\n");

        List<YangNode> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            appendNodeHierarchy(sb, children.get(i), childPrefix, i == children.size() - 1);
        }
    }

    private int[] countNodes(List<YangNode> nodes) {
        int[] counts = new int[4]; // total, containers, leaves, lists
        for (YangNode node : nodes) {
            counts[0]++;
            if ("container".equals(node.getType())) counts[1]++;
            else if ("leaf".equals(node.getType()) || "leaf-list".equals(node.getType())) counts[2]++;
            else if ("list".equals(node.getType())) counts[3]++;
            int[] childCounts = countNodes(node.getChildren());
            for (int i = 0; i < 4; i++) counts[i] += childCounts[i];
        }
        return counts;
    }
}
