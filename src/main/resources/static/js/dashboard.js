const toast = document.getElementById("toast");

const showToast = (message) => {
    toast.textContent = message;
    toast.classList.add("visible");
    window.setTimeout(() => toast.classList.remove("visible"), 2600);
};

const request = async (url, options = {}) => {
    const isFormData = options.body instanceof FormData;
    const response = await fetch(url, {
        headers: isFormData ? options.headers : {
            "Content-Type": "application/json",
            ...options.headers
        },
        ...options
    });

    if (!response.ok) {
        const contentType = response.headers.get("content-type") || "";
        if (contentType.includes("application/json")) {
            const errorJson = await response.json();
            throw new Error(errorJson.message || "Something went wrong");
        }
        const text = await response.text();
        throw new Error(text || "Something went wrong");
    }

    return response;
};

document.getElementById("lead-form")?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(event.target);
    const payload = Object.fromEntries(formData.entries());

    try {
        await request("/api/leads", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        showToast("Lead created successfully");
        window.location.reload();
    } catch (error) {
        showToast(error.message);
    }
});

document.getElementById("invoice-form")?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(event.target);
    const payload = Object.fromEntries(formData.entries());
    payload.amount = Number(payload.amount);
    payload.leadId = payload.leadId ? Number(payload.leadId) : null;

    try {
        await request("/api/invoices", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        showToast("Invoice created successfully");
        window.location.reload();
    } catch (error) {
        showToast(error.message);
    }
});

document.getElementById("csv-import-form")?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(event.target);

    try {
        const response = await request("/api/leads/import/csv", {
            method: "POST",
            body: formData
        });
        const result = await response.json();
        const message = `Imported ${result.importedCount} leads${result.skippedCount ? `, skipped ${result.skippedCount}` : ""}`;
        showToast(message);
        window.setTimeout(() => window.location.reload(), 900);
    } catch (error) {
        showToast(error.message);
    }
});

document.getElementById("user-form")?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(event.target);
    const payload = Object.fromEntries(formData.entries());

    try {
        await request("/api/users", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        showToast("User created successfully");
        window.setTimeout(() => window.location.reload(), 900);
    } catch (error) {
        showToast(error.message);
    }
});

document.getElementById("user-password-reset-form")?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(event.target);
    const userId = formData.get("userId");
    const payload = {
        newPassword: formData.get("newPassword")
    };

    try {
        await request(`/api/users/${userId}/password`, {
            method: "PATCH",
            body: JSON.stringify(payload)
        });
        showToast("Password reset successfully");
        event.target.reset();
    } catch (error) {
        showToast(error.message);
    }
});

document.getElementById("account-password-form")?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(event.target);
    const payload = Object.fromEntries(formData.entries());

    try {
        await request("/api/users/account/password", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        showToast("Password updated successfully");
        event.target.reset();
    } catch (error) {
        showToast(error.message);
    }
});

document.querySelectorAll(".lead-status-select").forEach((select) => {
    select.addEventListener("change", async (event) => {
        try {
            await request(`/api/leads/${event.target.dataset.id}/status`, {
                method: "PATCH",
                body: JSON.stringify({status: event.target.value})
            });
            showToast("Lead status updated");
            window.location.reload();
        } catch (error) {
            showToast(error.message);
        }
    });
});

document.querySelectorAll(".invoice-status-select").forEach((select) => {
    select.addEventListener("change", async (event) => {
        try {
            await request(`/api/invoices/${event.target.dataset.id}/status`, {
                method: "PATCH",
                body: JSON.stringify({status: event.target.value})
            });
            showToast("Invoice status updated");
            window.location.reload();
        } catch (error) {
            showToast(error.message);
        }
    });
});

document.querySelectorAll(".user-status-select").forEach((select) => {
    select.addEventListener("change", async (event) => {
        try {
            await request(`/api/users/${event.target.dataset.id}/status`, {
                method: "PATCH",
                body: JSON.stringify({active: event.target.value === "true"})
            });
            showToast("User access updated");
            window.setTimeout(() => window.location.reload(), 700);
        } catch (error) {
            showToast(error.message);
            window.setTimeout(() => window.location.reload(), 900);
        }
    });
});
