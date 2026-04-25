package com.yangparser.model;

import java.util.List;

public class YangModule {
    private String moduleName;
    private String namespace;
    private String prefix;
    private String organization;
    private String contact;
    private String description;
    private String yangVersion;
    private String revision;
    private List<YangNode> nodes;
    private String hierarchyText;
    private int totalNodes;
    private int leafCount;
    private int containerCount;
    private int listCount;

    public YangModule() {}

    // Getters and Setters
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getYangVersion() { return yangVersion; }
    public void setYangVersion(String yangVersion) { this.yangVersion = yangVersion; }

    public String getRevision() { return revision; }
    public void setRevision(String revision) { this.revision = revision; }

    public List<YangNode> getNodes() { return nodes; }
    public void setNodes(List<YangNode> nodes) { this.nodes = nodes; }

    public String getHierarchyText() { return hierarchyText; }
    public void setHierarchyText(String hierarchyText) { this.hierarchyText = hierarchyText; }

    public int getTotalNodes() { return totalNodes; }
    public void setTotalNodes(int totalNodes) { this.totalNodes = totalNodes; }

    public int getLeafCount() { return leafCount; }
    public void setLeafCount(int leafCount) { this.leafCount = leafCount; }

    public int getContainerCount() { return containerCount; }
    public void setContainerCount(int containerCount) { this.containerCount = containerCount; }

    public int getListCount() { return listCount; }
    public void setListCount(int listCount) { this.listCount = listCount; }
}
