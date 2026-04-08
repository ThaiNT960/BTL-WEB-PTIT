/**
 * @param {string} url - Đường dẫn URL (nên bao gồm CTX ở trước)
 * @param {object} options - Các tùy chọn fetch (method, headers, body...)
 * @param {boolean} returnJson - Mặc định true. Trả về promise parse JSON nếu true, ngược lại trả về response gốc.
 * @returns {Promise<any>}
 */
async function apiFetch(url, options = {}, returnJson = true) {
    try {
        const response = await fetch(url, options);

        if (!response.ok) {
            if (response.status === 401) {
                alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
                window.location.href = CTX + '/AuthServlet?action=login';
                throw new Error("Unauthorized");
            } else if (response.status === 400) {
                try {
                    const data = await response.json();
                    if (data.error) alert(data.error);
                } catch(e) {}
                throw new Error("Bad Request: " + response.statusText);
            } else if (response.status >= 500) {
                alert("Lỗi máy chủ " + response.status + ". Vui lòng thử lại sau!");
                throw new Error("Server Error");
            } else {
                console.error("Lỗi HTTP:", response.status, response.statusText);
            }
        }

        if (returnJson) {
            try {
                return await response.json();
            } catch (err) {
                return {};
            }
        }

        return response;
    } catch (error) {
        console.error("API Fetch Error:", error);
        throw error;
    }
}
