// js/main.js
document.addEventListener('DOMContentLoaded', function() {
    // 页面元素
    const paymentModal = document.getElementById('paymentOverlay');
    const buyBtn = document.getElementById('buyBtn');
    const groupBuyBtn = document.getElementById('groupBuyBtn');
    const cancelPaymentBtn = document.getElementById('cancelPaymentBtn');
    const confirmPaymentBtn = document.getElementById('confirmPaymentBtn');
    const notification = document.getElementById('notification');

    // 轮播图相关
    const swiperWrapper = document.getElementById('swiperWrapper');
    const swiperPagination = document.getElementById('swiperPagination');
//    const imageUrl = 'https://bugstack.cn/images/article/product/book/mybatis-03.png?raw=true';
    const imageList = [
      'images/sku-13811216-01.png',
      'images/sku-13811216-02.png',
      'images/sku-13811216-03.png',
    ];
    // 初始化轮播图
    function initSwiper() {
        swiperWrapper.innerHTML = '';
        swiperPagination.innerHTML = '';

        imageList.forEach((src, index) => {
            // slide
            const slide = document.createElement('div');
            slide.className = 'swiper-slide';

            const img = document.createElement('img');
            img.src = src;
            img.alt = `商品图片 ${index + 1}`;

            slide.appendChild(img);
            swiperWrapper.appendChild(slide);

            // pagination dot
            const dot = document.createElement('div');
            dot.className = index === 0 ? 'swiper-dot active' : 'swiper-dot';
            dot.dataset.index = index;
            swiperPagination.appendChild(dot);
        });

        let currentIndex = 0;
        const total = imageList.length;

        // 自动轮播
        setInterval(() => {
            currentIndex = (currentIndex + 1) % total;
            swiperWrapper.style.transform = `translateX(-${currentIndex * 100}%)`;

            document.querySelectorAll('.swiper-dot').forEach((dot, i) => {
                dot.classList.toggle('active', i === currentIndex);
            });
        }, 3000);

        // 点击分页点
        document.querySelectorAll('.swiper-dot').forEach(dot => {
            dot.addEventListener('click', function () {
                currentIndex = Number(this.dataset.index);
                swiperWrapper.style.transform = `translateX(-${currentIndex * 100}%)`;

                document.querySelectorAll('.swiper-dot').forEach((d, i) => {
                    d.classList.toggle('active', i === currentIndex);
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

    // 初始化轮播图
    initSwiper();

    // 检查登录状态
    checkLoginStatus();
});