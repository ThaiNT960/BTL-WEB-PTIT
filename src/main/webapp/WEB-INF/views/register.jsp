<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="vi">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>PTIT Social Chat - Đăng Ký</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <style>
            .text-ptit-red {
                color: #ae1a21;
            }

            .bg-ptit-red {
                background: linear-gradient(135deg, #ae1a21 0%, #8a141a 100%);
            }

            .border-ptit-red {
                border-color: #ae1a21;
            }
        </style>
    </head>

    <body class="bg-gradient-to-br from-gray-50 to-gray-100 min-h-screen flex items-center justify-center p-4">
        <div class="w-full max-w-md">
            <div class="bg-white rounded-lg shadow-2xl p-8 border-t-4 border-ptit-red">
                <div class="text-center mb-8">
                    <h1 class="text-3xl font-bold text-ptit-red">PTIT Social Chat</h1>
                    <p class="text-gray-600 mt-2 text-sm">Tạo tài khoản mới</p>
                </div>

                <% if (request.getAttribute("error") !=null) { %>
                    <div class="bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-3 mb-4 text-sm">
                        <%= request.getAttribute("error") %>
                    </div>
                    <% } %>

                        <form method="post" action="${pageContext.request.contextPath}/RegisterServlet"
                            class="space-y-4">
                            <div>
                                <label class="block text-gray-700 font-semibold mb-2 text-sm">Tên đăng nhập</label>
                                <input type="text" name="username"
                                    class="w-full px-4 py-3 border-2 border-gray-200 rounded-lg focus:outline-none focus:border-ptit-red transition"
                                    placeholder="Nhập tên đăng nhập" required>
                            </div>
                            <div>
                                <label class="block text-gray-700 font-semibold mb-2 text-sm">Họ và tên</label>
                                <input type="text" name="fullName"
                                    class="w-full px-4 py-3 border-2 border-gray-200 rounded-lg focus:outline-none focus:border-ptit-red transition"
                                    placeholder="Nhập họ và tên">
                            </div>
                            <div>
                                <label class="block text-gray-700 font-semibold mb-2 text-sm">Mật khẩu</label>
                                <input type="password" name="password"
                                    class="w-full px-4 py-3 border-2 border-gray-200 rounded-lg focus:outline-none focus:border-ptit-red transition"
                                    placeholder="Nhập mật khẩu" required>
                            </div>
                            <button type="submit"
                                class="w-full bg-ptit-red text-white font-semibold py-3 rounded-lg hover:opacity-90 transition shadow-lg">Đăng
                                Ký</button>
                        </form>

                        <div class="mt-6 text-center">
                            <p class="text-gray-600 text-sm">Đã có tài khoản?
                                <a href="${pageContext.request.contextPath}/LoginServlet"
                                    class="text-ptit-red font-semibold hover:underline">Đăng nhập</a>
                            </p>
                        </div>
            </div>
        </div>
    </body>

    </html>