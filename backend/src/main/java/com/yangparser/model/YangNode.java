package com.yangparser.model;

import java.util.ArrayList;
import java.util.List;

public class YangNode {
    private String name;
    private String type;         // container, leaf, list, leaf-list, choice, case, grouping, etc.
    private String dataType;     // string, int32, boolean, etc. (for leaf/leaf-list)
    private String description;
    private String mandatory;
    private String defaultValue;
    private String pattern;      // regex pattern constraint
    private String range;        // numeric range constraint
    private String minElements;
    private String maxElements;
    private String key;          // list key
    private List<YangNode> children;
    private int depth;

    public YangNode() {
        this.children = new ArrayList<>();
    }

    public YangNode(String name, String type, int depth) {
        this.name = name;
        this.type = type;
        this.depth = depth;
        this.children = new ArrayList<>();
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMandatory() { return mandatory; }
    public void setMandatory(String mandatory) { this.mandatory = mandatory; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }

    public String getMinElements() { return minElements; }
    public void setMinElements(String minElements) { this.minElements = minElements; }

    public String getMaxElements() { return maxElements; }
    public void setMaxElements(String maxElements) { this.maxElements = maxElements; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public List<YangNode> getChildren() { return children; }
    public void setChildren(List<YangNode> children) { this.children = children; }

    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }

    public void addChild(YangNode child) {
        this.children.add(child);
    }

    /**
     * Returns a human-readable hierarchical string representation
     */
    public String toHierarchyString() {
        StringBuilder sb = new StringBuilder();
        buildHierarchy(sb, "", true);
        return sb.toString();
    }

    private void buildHierarchy(StringBuilder sb, String prefix, boolean isLast) {
        String connector = isLast ? "└── " : "├── ";
        sb.append(prefix).append(connector);
        sb.append("[").append(type.toUpperCase()).append("] ").append(name);

        if (dataType != null && !dataType.isEmpty()) {
            sb.append(" : ").append(dataType);
        }
        if (mandatory != null && mandatory.equals("true")) {
            sb.append(" *");
        }
        sb.append("\n");

        String childPrefix = prefix + (isLast ? "    " : "│   ");

        // Print constraints
        if (description != null && !description.isEmpty()) {
            sb.append(childPrefix).append("    desc: ").append(description).append("\n");
        }
        if (range != null && !range.isEmpty()) {
            sb.append(childPrefix).append("    range: ").append(range).append("\n");
        }
        if (pattern != null && !pattern.isEmpty()) {
            sb.append(childPrefix).append("    pattern: ").append(pattern).append("\n");
        }
        if (key != null && !key.isEmpty()) {
            sb.append(childPrefix).append("    key: ").append(key).append("\n");
        }

        for (int i = 0; i < children.size(); i++) {
            children.get(i).buildHierarchy(sb, childPrefix, i == children.size() - 1);
        }
    }
}
