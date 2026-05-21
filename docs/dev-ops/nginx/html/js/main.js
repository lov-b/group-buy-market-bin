// js/main.js
document.addEventListener('DOMContentLoaded', function() {
    // 页面元素
    const paymentModal = document.getElementById('paymentModal');
    const buyBtn = document.getElementById('buyBtn');
    const groupBuyBtn = document.getElementById('groupBuyBtn');
    const cancelPaymentBtn = document.getElementById('cancelPaymentBtn');
    const confirmPaymentBtn = document.getElementById('confirmPaymentBtn');
    const notification = document.getElementById('notification');

    // 轮播图相关
    const swiperWrapper = document.getElementById('swiperWrapper');
    const swiperPagination = document.getElementById('swiperPagination');
    const imageUrl = 'https://bugstack.cn/images/article/product/book/mybatis-03.png?raw=true';

    // 初始化轮播图
    function initSwiper() {
        // 创建3个轮播图项
        for (let i = 0; i < 3; i++) {
            const slide = document.createElement('div');
            slide.className = 'swiper-slide';

            const img = document.createElement('img');
            img.src = imageUrl;
            img.alt = `手写MyBatis ${i+1}`;

            slide.appendChild(img);
            swiperWrapper.appendChild(slide);

            // 创建分页点
            const dot = document.createElement('div');
            dot.className = i === 0 ? 'swiper-dot active' : 'swiper-dot';
            dot.dataset.index = i;
            swiperPagination.appendChild(dot);
        }

        // 当前轮播图索引
        let currentIndex = 0;

        // 自动轮播
        setInterval(() => {
            currentIndex = (currentIndex + 1) % 3;
            swiperWrapper.style.transform = `translateX(-${currentIndex * 100}%)`;

            // 更新分页点
            document.querySelectorAll('.swiper-dot').forEach((dot, index) => {
                dot.classList.toggle('active', index === currentIndex);
            });
        }, 3000);

        // 点击分页点切换
        document.querySelectorAll('.swiper-dot').forEach(dot => {
            dot.addEventListener('click', function() {
                const index = parseInt(this.dataset.index);
                currentIndex = index;
                swiperWrapper.style.transform = `translateX(-${currentIndex * 100}%)`;

                // 更新分页点
                document.querySelectorAll('.swiper-dot').forEach((dot, i) => {
                    dot.classList.toggle('active', i === currentIndex);
                });
            });
        });
    }

    // 显示通知
    function showNotification(message) {
        notification.textContent = message;
        notification.style.display = 'block';

        setTimeout(() => {
            notification.style.display = 'none';
        }, 3000);
    }

    // 直接购买按钮点击
    buyBtn.addEventListener('click', function() {
        paymentModal.style.display = 'flex';
    });

    // 开团购买按钮点击
    groupBuyBtn.addEventListener('click', function() {
        paymentModal.style.display = 'flex';
    });

    // 取消支付按钮点击
    cancelPaymentBtn.addEventListener('click', function() {
        paymentModal.style.display = 'none';
        showNotification('已取消支付');
    });

    // 支付完成按钮点击
    confirmPaymentBtn.addEventListener('click', function() {
        paymentModal.style.display = 'none';
        showNotification('支付成功！订单已生成');
    });

    // 立即抢单按钮点击
    document.querySelectorAll('.group-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            paymentModal.style.display = 'flex';
        });
    });

    // 点击弹窗外区域关闭弹窗
    paymentModal.addEventListener('click', function(e) {
        if (e.target === paymentModal) {
            paymentModal.style.display = 'none';
        }
    });

    // 检查登录状态
    function checkLoginStatus() {
        const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        if (!isLoggedIn) {
            window.location.href = 'login.html';
        }
    }

    // 初始化轮播图
    initSwiper();

    // 检查登录状态
    checkLoginStatus();
});