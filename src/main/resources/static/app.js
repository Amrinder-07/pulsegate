const state = {
    activeKey: localStorage.getItem("pulsegate.activeKey") || ""
};

const elements = {
    keyForm: document.querySelector("#keyForm"),
    ownerName: document.querySelector("#ownerName"),
    activeKey: document.querySelector("#activeKey"),
    callButton: document.querySelector("#callButton"),
    stressButton: document.querySelector("#stressButton"),
    resetButton: document.querySelector("#resetButton"),
    refreshButton: document.querySelector("#refreshButton"),
    totalRequests: document.querySelector("#totalRequests"),
    successfulRequests: document.querySelector("#successfulRequests"),
    blockedRequests: document.querySelector("#blockedRequests"),
    failedRequests: document.querySelector("#failedRequests"),
    averageLatency: document.querySelector("#averageLatency"),
    activeApiKeys: document.querySelector("#activeApiKeys"),
    keysBody: document.querySelector("#keysBody"),
    logsBody: document.querySelector("#logsBody"),
    lastResult: document.querySelector("#lastResult"),
    rateStatus: document.querySelector("#rateStatus")
};

elements.keyForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const ownerName = elements.ownerName.value.trim();
    if (!ownerName) return;

    const response = await fetch("/api/keys", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({ownerName})
    });

    if (!response.ok) {
        setResult(`Key creation failed: ${response.status}`);
        return;
    }

    const key = await response.json();
    state.activeKey = key.apiKey;
    localStorage.setItem("pulsegate.activeKey", key.apiKey);
    elements.ownerName.value = "";
    setResult("Generated new API key");
    await refresh();
});

elements.callButton.addEventListener("click", async () => {
    await callProtected();
    await refresh();
});

elements.stressButton.addEventListener("click", async () => {
    if (!state.activeKey) {
        setResult("Generate an API key first");
        return;
    }

    elements.stressButton.disabled = true;
    setResult("Sending 20 requests...");

    const calls = Array.from({length: 20}, () => callProtected(false));
    const results = await Promise.all(calls);
    const blocked = results.filter((status) => status === 429).length;

    setResult(`Traffic burst complete: ${blocked} blocked`);
    elements.stressButton.disabled = false;
    await refresh();
});

elements.refreshButton.addEventListener("click", refresh);

elements.resetButton.addEventListener("click", async () => {
    const confirmed = window.confirm("Clear all API keys and request logs for this demo?");
    if (!confirmed) return;

    const response = await fetch("/api/demo-data", {method: "DELETE"});
    if (!response.ok) {
        setResult(`Reset failed: ${response.status}`);
        return;
    }

    state.activeKey = "";
    localStorage.removeItem("pulsegate.activeKey");
    setResult("Demo data cleared");
    await refresh();
});

async function callProtected(showResult = true) {
    if (!state.activeKey) {
        setResult("Generate an API key first");
        return null;
    }

    const response = await fetch("/api/protected", {
        headers: {"X-API-Key": state.activeKey}
    });

    if (showResult) {
        setResult(response.status === 200 ? "Protected request accepted" : `Protected request returned ${response.status}`);
    }
    return response.status;
}

async function refresh() {
    renderActiveKey();
    await Promise.all([loadSummary(), loadKeys(), loadLogs()]);
}

async function loadSummary() {
    const response = await fetch("/api/dashboard/summary");
    const summary = await response.json();

    elements.totalRequests.textContent = summary.totalRequests;
    elements.successfulRequests.textContent = summary.successfulRequests;
    elements.blockedRequests.textContent = summary.blockedRequests;
    elements.failedRequests.textContent = summary.failedRequests;
    elements.averageLatency.textContent = `${summary.averageLatencyMs} ms`;
    elements.activeApiKeys.textContent = summary.activeApiKeys;
}

async function loadKeys() {
    const response = await fetch("/api/keys");
    const keys = await response.json();

    if (keys.length === 0) {
        elements.keysBody.innerHTML = `<tr><td colspan="5">No API keys yet.</td></tr>`;
        return;
    }

    elements.keysBody.innerHTML = keys.map((key) => `
        <tr>
            <td>${escapeHtml(key.ownerName)}</td>
            <td><code class="key-chip" title="${escapeHtml(key.apiKey)}">${escapeHtml(previewKey(key.apiKey))}</code></td>
            <td>${key.totalRequests}</td>
            <td>${key.currentWindowRequests}</td>
            <td>${key.remainingThisMinute}</td>
        </tr>
    `).join("");

    const active = keys.find((key) => key.apiKey === state.activeKey);
    if (active) {
        elements.rateStatus.textContent = `Current key: ${active.remainingThisMinute}/10 remaining`;
    } else {
        elements.rateStatus.textContent = "Limit: 10/min";
    }
}

async function loadLogs() {
    const response = await fetch("/api/logs?limit=50");
    const logs = await response.json();

    if (logs.length === 0) {
        elements.logsBody.innerHTML = `<tr><td colspan="6">No requests logged yet.</td></tr>`;
        return;
    }

    elements.logsBody.innerHTML = logs.map((log) => {
        const badge = statusBadge(log.statusCode, log.blocked);
        return `
            <tr>
                <td>${formatTime(log.timestamp)}</td>
                <td><code class="key-chip">${escapeHtml(log.apiKeyPreview)}</code></td>
                <td>${escapeHtml(log.method)} ${escapeHtml(log.path)}</td>
                <td>${badge}</td>
                <td>${log.latencyMs} ms</td>
                <td>${log.blocked ? "Yes" : "No"}</td>
            </tr>
        `;
    }).join("");
}

function renderActiveKey() {
    elements.activeKey.textContent = state.activeKey || "No key generated yet";
}

function setResult(message) {
    elements.lastResult.textContent = message;
}

function previewKey(key) {
    if (!key || key.length <= 16) return key || "missing";
    return `${key.slice(0, 12)}...${key.slice(-4)}`;
}

function formatTime(value) {
    return new Intl.DateTimeFormat(undefined, {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit"
    }).format(new Date(value));
}

function statusBadge(status, blocked) {
    if (blocked || status === 429) {
        return `<span class="badge warn">${status}</span>`;
    }
    if (status >= 200 && status < 300) {
        return `<span class="badge success">${status}</span>`;
    }
    return `<span class="badge danger">${status}</span>`;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

refresh();
