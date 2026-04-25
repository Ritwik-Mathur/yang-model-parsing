import { useState, useRef } from "react";

const API_BASE = "/api/yang";

const NODE_COLORS = {
  container: { bg: "#1e3a5f", border: "#3b82f6", text: "#93c5fd", icon: "⬡" },
  list: { bg: "#1e3a2f", border: "#22c55e", text: "#86efac", icon: "⊞" },
  leaf: { bg: "#2d1f3d", border: "#a855f7", text: "#d8b4fe", icon: "◈" },
  "leaf-list": { bg: "#3d2f1f", border: "#f97316", text: "#fed7aa", icon: "◉" },
  choice: { bg: "#3d2020", border: "#ef4444", text: "#fca5a5", icon: "◇" },
  case: { bg: "#2d3520", border: "#84cc16", text: "#bef264", icon: "◆" },
  grouping: { bg: "#2d2d2d", border: "#6b7280", text: "#d1d5db", icon: "◎" },
  default: { bg: "#1e293b", border: "#475569", text: "#94a3b8", icon: "○" },
};

function Badge({ text, color = "#3b82f6" }) {
  return (
    <span style={{
      display: "inline-block",
      padding: "1px 8px",
      borderRadius: "999px",
      fontSize: "10px",
      fontWeight: "700",
      letterSpacing: "0.05em",
      background: color + "22",
      border: `1px solid ${color}55`,
      color: color,
      marginLeft: "6px",
      fontFamily: "monospace"
    }}>{text}</span>
  );
}

function NodeCard({ node, depth = 0 }) {
  const [expanded, setExpanded] = useState(depth < 2);
  const colors = NODE_COLORS[node.type] || NODE_COLORS.default;
  const hasChildren = node.children && node.children.length > 0;
  const hasConstraints = node.dataType || node.range || node.pattern || node.key ||
    node.mandatory === "true" || node.defaultValue || node.description;

  return (
    <div style={{
      marginLeft: depth === 0 ? 0 : "20px",
      marginBottom: "6px",
      position: "relative"
    }}>
      {depth > 0 && (
        <div style={{
          position: "absolute",
          left: "-14px",
          top: "0",
          bottom: "0",
          width: "1px",
          background: "#1e3a5f",
        }} />
      )}

      <div
        onClick={() => (hasChildren || hasConstraints) && setExpanded(!expanded)}
        style={{
          background: colors.bg,
          border: `1px solid ${colors.border}33`,
          borderLeft: `3px solid ${colors.border}`,
          borderRadius: "6px",
          padding: "8px 12px",
          cursor: (hasChildren || hasConstraints) ? "pointer" : "default",
          transition: "all 0.15s ease",
          userSelect: "none",
        }}
        onMouseEnter={e => {
          if (hasChildren || hasConstraints) {
            e.currentTarget.style.borderLeftColor = colors.border;
            e.currentTarget.style.background = colors.bg + "ee";
          }
        }}
        onMouseLeave={e => {
          e.currentTarget.style.borderLeftColor = colors.border + "33";
          e.currentTarget.style.background = colors.bg;
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "8px", flexWrap: "wrap" }}>
          <span style={{ color: colors.border, fontSize: "13px" }}>{colors.icon}</span>
          <span style={{ color: colors.text, fontWeight: "600", fontFamily: "monospace", fontSize: "13px" }}>
            {node.name}
          </span>
          <Badge text={node.type} color={colors.border} />
          {node.dataType && <Badge text={node.dataType} color="#f59e0b" />}
          {node.mandatory === "true" && <Badge text="mandatory" color="#ef4444" />}
          {node.key && <Badge text={`key: ${node.key}`} color="#22c55e" />}
          {hasChildren && (
            <span style={{ marginLeft: "auto", color: "#475569", fontSize: "11px" }}>
              {expanded ? "▲" : "▼"} {node.children.length}
            </span>
          )}
        </div>

        {expanded && hasConstraints && (
          <div style={{
            marginTop: "8px",
            paddingTop: "8px",
            borderTop: `1px solid ${colors.border}22`,
            display: "flex",
            flexDirection: "column",
            gap: "3px"
          }}>
            {node.description && (
              <div style={{ fontSize: "11px", color: "#64748b", fontStyle: "italic" }}>
                ℹ {node.description}
              </div>
            )}
            {node.range && (
              <div style={{ fontSize: "11px", color: "#f59e0b", fontFamily: "monospace" }}>
                range: {node.range}
              </div>
            )}
            {node.pattern && (
              <div style={{ fontSize: "11px", color: "#a855f7", fontFamily: "monospace" }}>
                pattern: {node.pattern}
              </div>
            )}
            {node.defaultValue && (
              <div style={{ fontSize: "11px", color: "#22c55e", fontFamily: "monospace" }}>
                default: {node.defaultValue}
              </div>
            )}
          </div>
        )}
      </div>

      {expanded && hasChildren && (
        <div style={{ marginTop: "4px" }}>
          {node.children.map((child, idx) => (
            <NodeCard key={idx} node={child} depth={depth + 1} />
          ))}
        </div>
      )}
    </div>
  );
}

function StatCard({ label, value, color }) {
  return (
    <div style={{
      background: "#0f172a",
      border: `1px solid ${color}33`,
      borderRadius: "8px",
      padding: "12px 16px",
      textAlign: "center",
      flex: 1
    }}>
      <div style={{ fontSize: "24px", fontWeight: "800", color, fontFamily: "monospace" }}>{value}</div>
      <div style={{ fontSize: "11px", color: "#475569", marginTop: "2px", letterSpacing: "0.05em" }}>{label}</div>
    </div>
  );
}

export default function App() {
  const [yangInput, setYangInput] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState("tree");
  const fileInputRef = useRef(null);

  const callApi = async (url, options) => {
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const res = await fetch(url, options);
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Request failed");
      setResult(data);
      setActiveTab("tree");
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleParse = () => {
    if (!yangInput.trim()) return setError("Please enter or paste YANG content.");
    callApi(`${API_BASE}/parse`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ content: yangInput }),
    });
  };

  const handleLoadSample = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/sample`);
      const data = await res.json();
      setYangInput(data.content);
      setError(null);
    } catch {
      setError("Could not load sample — is the backend running?");
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const fd = new FormData();
    fd.append("file", file);
    callApi(`${API_BASE}/upload`, { method: "POST", body: fd });
  };

  const tabStyle = (tab) => ({
    padding: "6px 16px",
    borderRadius: "6px",
    cursor: "pointer",
    fontSize: "12px",
    fontWeight: "600",
    letterSpacing: "0.05em",
    background: activeTab === tab ? "#1e3a5f" : "transparent",
    color: activeTab === tab ? "#93c5fd" : "#475569",
    border: activeTab === tab ? "1px solid #3b82f633" : "1px solid transparent",
    transition: "all 0.15s",
  });

  return (
    <div style={{
      minHeight: "100vh",
      background: "#020617",
      color: "#e2e8f0",
      fontFamily: "'JetBrains Mono', 'Fira Code', 'Cascadia Code', monospace",
      display: "flex",
      flexDirection: "column",
    }}>
      {/* Header */}
      <div style={{
        background: "#0a0f1e",
        borderBottom: "1px solid #1e293b",
        padding: "16px 32px",
        display: "flex",
        alignItems: "center",
        gap: "16px"
      }}>
        <div style={{
          width: "36px", height: "36px",
          background: "linear-gradient(135deg, #3b82f6, #8b5cf6)",
          borderRadius: "8px",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          fontSize: "18px"
        }}>⬡</div>
        <div>
          <div style={{ fontWeight: "800", fontSize: "16px", letterSpacing: "0.05em" }}>
            YANG Model Parser
          </div>
          <div style={{ fontSize: "11px", color: "#475569" }}>
            Spring Boot Backend · RFC 7950 · Visual Hierarchy Explorer
          </div>
        </div>
        <div style={{ marginLeft: "auto", display: "flex", gap: "8px" }}>
          <div style={{
            width: "8px", height: "8px", borderRadius: "50%",
            background: result ? "#22c55e" : "#475569",
            boxShadow: result ? "0 0 6px #22c55e" : "none"
          }} />
          <span style={{ fontSize: "11px", color: "#475569" }}>
            {result ? "Model Loaded" : "No Model"}
          </span>
        </div>
      </div>

      <div style={{ display: "flex", flex: 1, overflow: "hidden" }}>
        {/* Left: Editor Panel */}
        <div style={{
          width: "420px",
          minWidth: "320px",
          display: "flex",
          flexDirection: "column",
          borderRight: "1px solid #1e293b",
          background: "#0a0f1e",
        }}>
          <div style={{ padding: "16px", borderBottom: "1px solid #1e293b" }}>
            <div style={{ fontSize: "11px", color: "#475569", marginBottom: "10px", letterSpacing: "0.08em" }}>
              YANG SOURCE
            </div>
            <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
              <button
                onClick={handleLoadSample}
                disabled={loading}
                style={{
                  flex: 1, padding: "7px", borderRadius: "6px",
                  background: "#1e293b", border: "1px solid #334155",
                  color: "#94a3b8", cursor: "pointer", fontSize: "11px", fontWeight: "600"
                }}
              >⬇ Load Sample</button>
              <button
                onClick={() => fileInputRef.current?.click()}
                disabled={loading}
                style={{
                  flex: 1, padding: "7px", borderRadius: "6px",
                  background: "#1e293b", border: "1px solid #334155",
                  color: "#94a3b8", cursor: "pointer", fontSize: "11px", fontWeight: "600"
                }}
              >📁 Upload .yang</button>
              <input
                ref={fileInputRef}
                type="file"
                accept=".yang"
                style={{ display: "none" }}
                onChange={handleFileUpload}
              />
            </div>
          </div>

          <textarea
            value={yangInput}
            onChange={e => setYangInput(e.target.value)}
            placeholder="Paste your YANG model here..."
            style={{
              flex: 1,
              background: "transparent",
              border: "none",
              color: "#94a3b8",
              fontFamily: "inherit",
              fontSize: "12px",
              lineHeight: "1.7",
              resize: "none",
              padding: "16px",
              outline: "none",
              overflowY: "auto"
            }}
          />

          <div style={{ padding: "12px 16px", borderTop: "1px solid #1e293b" }}>
            {error && (
              <div style={{
                padding: "8px 12px", borderRadius: "6px",
                background: "#7f1d1d22", border: "1px solid #ef444433",
                color: "#fca5a5", fontSize: "11px", marginBottom: "10px"
              }}>⚠ {error}</div>
            )}
            <button
              onClick={handleParse}
              disabled={loading || !yangInput.trim()}
              style={{
                width: "100%", padding: "10px",
                borderRadius: "8px",
                background: loading ? "#1e293b" : "linear-gradient(135deg, #3b82f6, #8b5cf6)",
                border: "none",
                color: loading ? "#475569" : "white",
                cursor: loading || !yangInput.trim() ? "not-allowed" : "pointer",
                fontWeight: "800", fontSize: "13px",
                letterSpacing: "0.05em",
                transition: "all 0.2s",
                fontFamily: "inherit"
              }}
            >
              {loading ? "◌ Parsing..." : "▶ Parse YANG Model"}
            </button>
          </div>
        </div>

        {/* Right: Results Panel */}
        <div style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
          {!result && !loading && (
            <div style={{
              flex: 1, display: "flex", flexDirection: "column",
              alignItems: "center", justifyContent: "center", color: "#1e293b"
            }}>
              <div style={{ fontSize: "64px", marginBottom: "16px" }}>⬡</div>
              <div style={{ fontSize: "14px", color: "#334155" }}>
                Load a sample or paste your YANG content to begin
              </div>
            </div>
          )}

          {loading && (
            <div style={{
              flex: 1, display: "flex", alignItems: "center",
              justifyContent: "center", color: "#3b82f6"
            }}>
              <div style={{ textAlign: "center" }}>
                <div style={{ fontSize: "32px", animation: "spin 1s linear infinite" }}>◌</div>
                <div style={{ marginTop: "12px", fontSize: "12px" }}>Parsing model...</div>
              </div>
            </div>
          )}

          {result && (
            <>
              {/* Module Info */}
              <div style={{
                padding: "16px 24px",
                borderBottom: "1px solid #1e293b",
                background: "#0a0f1e"
              }}>
                <div style={{ display: "flex", alignItems: "center", gap: "12px", marginBottom: "12px" }}>
                  <span style={{
                    fontWeight: "800", fontSize: "16px", color: "#e2e8f0"
                  }}>module: {result.moduleName}</span>
                  {result.revision && <Badge text={result.revision} color="#64748b" />}
                  {result.yangVersion && <Badge text={`YANG ${result.yangVersion}`} color="#3b82f6" />}
                </div>
                {result.namespace && (
                  <div style={{ fontSize: "11px", color: "#475569", marginBottom: "8px" }}>
                    ns: {result.namespace}
                  </div>
                )}
                <div style={{ display: "flex", gap: "8px" }}>
                  <StatCard label="TOTAL NODES" value={result.totalNodes} color="#3b82f6" />
                  <StatCard label="CONTAINERS" value={result.containerCount} color="#22c55e" />
                  <StatCard label="LEAVES" value={result.leafCount} color="#a855f7" />
                  <StatCard label="LISTS" value={result.listCount} color="#f97316" />
                </div>
              </div>

              {/* Tabs */}
              <div style={{
                display: "flex", gap: "4px", padding: "10px 24px",
                borderBottom: "1px solid #1e293b", background: "#0a0f1e"
              }}>
                <button style={tabStyle("tree")} onClick={() => setActiveTab("tree")}>
                  ⬡ Tree View
                </button>
                <button style={tabStyle("text")} onClick={() => setActiveTab("text")}>
                  ≡ Hierarchy Text
                </button>
              </div>

              {/* Tree View */}
              {activeTab === "tree" && (
                <div style={{ flex: 1, overflowY: "auto", padding: "16px 24px" }}>
                  {result.nodes?.map((node, idx) => (
                    <NodeCard key={idx} node={node} depth={0} />
                  ))}
                </div>
              )}

              {/* Text View */}
              {activeTab === "text" && (
                <div style={{ flex: 1, overflowY: "auto", padding: "16px 24px" }}>
                  <pre style={{
                    background: "#0a0f1e",
                    border: "1px solid #1e293b",
                    borderRadius: "8px",
                    padding: "16px",
                    fontSize: "12px",
                    lineHeight: "1.8",
                    color: "#94a3b8",
                    overflowX: "auto",
                    whiteSpace: "pre"
                  }}>{result.hierarchyText}</pre>
                </div>
              )}
            </>
          )}
        </div>
      </div>

      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600;800&display=swap');
        * { box-sizing: border-box; margin: 0; padding: 0; }
        ::-webkit-scrollbar { width: 4px; }
        ::-webkit-scrollbar-track { background: transparent; }
        ::-webkit-scrollbar-thumb { background: #1e3a5f; border-radius: 2px; }
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
      `}</style>
    </div>
  );
}
