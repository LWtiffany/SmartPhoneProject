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
            time = '10:00-12:00'
            try:
                # 创建预约记录时不再保存 guide 字段
                booking = Booking.objects.create(
                    name=name,
                    email=email,
                    date=date,
                    time_slot=time_slot,
                )
                if time_slot == 'A':
                    time = '9:30-12:30'
                if time_slot == 'B':
                    time = '12:30-15:30'
                if time_slot == 'C':
                    time = '15:30-18:30'
                send_success_email(email, name, date, time)  # 只发包含 name 的邮件

                logger.info(f'预约成功：{name} ({email})，{date}，时段{time_slot}')
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
