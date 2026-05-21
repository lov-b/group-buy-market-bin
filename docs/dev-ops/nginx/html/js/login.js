// js/login.js
document.addEventListener('DOMContentLoaded', function() {
    // 页面元素
    const loginBtn = document.getElementById('loginBtn');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const notification = document.getElementById('notification');

    // 显示通知
    function showNotification(message) {
        notification.textContent = message;
        notification.style.display = 'block';

        setTimeout(() => {
            notification.style.display = 'none';
        }, 3000);
    }

    // 登录功能
    loginBtn.addEventListener('click', function() {
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();

        if (!username || !password) {
            showNotification('请输入用户名和密码');
            return;
        }

        // 模拟登录成功
        showNotification('登录成功，正在跳转...');

        // 保存登录状态
        localStorage.setItem('isLoggedIn', 'true');
        localStorage.setItem('username', username);

        setTimeout(() => {
            window.location.href = 'index.html';
        }, 1000);
    });

    // 为输入框添加回车登录功能
    usernameInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            loginBtn.click();
        }
    });

    passwordInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            loginBtn.click();
        }
    });

    // 如果已经登录，直接跳转到主页面
    if (localStorage.getItem('isLoggedIn') === 'true') {
        window.location.href = 'index.html';
    }
});