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

            if Booking.objects.filter(email=email).exists():
                logger.info(f'重复预约尝试：{email}')
                return JsonResponse({'message': 'This email has already booked a tour.'}, status=400)

            try:
                booking = Booking.objects.create(
                    name=name,
                    email=email,
                    date=date,
                    time_slot=time_slot,
                )

                # ✅ 发邮件使用 name
                send_success_email(email, name)

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

