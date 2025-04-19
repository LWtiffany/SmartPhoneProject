from django.http import JsonResponse
from django.shortcuts import render
from Tourists_in_HKU.Form import BookingForm
from Tourists_in_HKU.models import Booking
from .SendEmail import send_success_email
import logging

logger = logging.getLogger('Tourists_in_HKU')


def book_tour_service(request):
    if request.method == 'POST':
        form = BookingForm(request.POST)

        if form.is_valid():
            name = form.cleaned_data['name']
            email = form.cleaned_data['email']
            date = form.cleaned_data['date']
            time_slot = form.cleaned_data['time_slot']
            guide = form.cleaned_data['guide']  # ✅ 获取 guide 字段（是否需要导游）

            # 你可以选择是否保留邮箱重复验证逻辑
            # if Booking.objects.filter(email=email).exists():
            #     logger.info(f'重复预约尝试：{email}')
            #     return JsonResponse({'message': 'This email has already booked a tour.'}, status=400)

            try:
                # ✅ 创建预约记录时，保存 guide 字段
                booking = Booking.objects.create(
                    name=name,
                    email=email,
                    date=date,
                    time_slot=time_slot,
                    guide=guide,  # ✅ 保存 guide 字段
                )

                # ✅ 发邮件时传递 guide 信息（告知用户是否需要导游）
                send_success_email(email, name)

                logger.info(f'预约成功：{name} ({email})，{date}，时段{time_slot}，需要导游: {guide}')
                return JsonResponse({'message': 'Booking successful!'}, status=200)

            except Exception as e:
                logger.error(f'预约失败：{str(e)}')
                return JsonResponse({'message': 'Server Error'}, status=500)

        else:
            logger.warning('表单验证失败')
            return JsonResponse({'message': 'Invalid input'}, status=400)

    elif request.method == 'GET':
        form = BookingForm()
        return render(request, 'Tourists_in_HKU/book_tour.html', {'form': form})

    else:
        logger.warning('请求方法不允许')
        return JsonResponse({'message': 'Wrong method'}, status=405)

