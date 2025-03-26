import logging
from django.http import HttpResponse
from django.shortcuts import render
from Tourists_in_HKU.Form import BookingForm
from .SendEmail import send_success_email

# 获取日志记录器
logger = logging.getLogger('Tourists_in_HKU')


def book_tour_service(request):
    """
    Booking Function Implementation
    """
    if request.method == 'POST':
        form = BookingForm(request.POST)

        # 判断输入是否合法
        if form.is_valid():
            try:
                # 保存表单数据到数据库
                booking = form.save()
                # 提取邮箱和用户名
                email = form.cleaned_data['email']
                username = form.cleaned_data['name']
                # 异步发送邮件，避免阻塞
                send_success_email.delay(email, username)
                logger.info(f'预约成功：{username} ({email})')

                return HttpResponse('Booking completed!', status=200)
            except Exception as e:
                logger.error(f'预约失败：{str(e)}')
                return HttpResponse('Server Error', status=500)
        else:
            logger.warning('表单验证失败')
            return HttpResponse('Invalid input', status=400)

    elif request.method == 'GET':
        # GET 方法仅在开发环境测试
        form = BookingForm()
        return render(request, 'Tourists_in_HKU/book_tour.html', {'form': form})

    else:
        logger.warning('请求方法不允许')
        return HttpResponse("Wrong method", status=405)
