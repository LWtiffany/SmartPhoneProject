from django.views.decorators.http import require_GET
from django.http import JsonResponse
from Tourists_in_HKU.models import Booking
import logging

logger = logging.getLogger('Tourists_in_HKU')

@require_GET
def get_bookings_by_email(request):
    """
    查询指定邮箱的所有预约记录
    参数：email=xxx@example.com
    """
    email = request.GET.get('email')

    if not email:
        return JsonResponse({'status': 'error', 'message': 'Email is required'}, status=400)

    try:
        bookings = Booking.objects.filter(email=email).values('name', 'date', 'time_slot')

        booking_list = list(bookings)

        if not booking_list:
            return JsonResponse({
                'status': 'success',
                'email': email,
                'bookings': [],
                'message': 'No bookings found for this email.'
            })

        return JsonResponse({
            'status': 'success',
            'email': email,
            'bookings': booking_list
        })

    except Exception as e:
        logger.error(f"查询邮箱预约失败：{str(e)}")
        return JsonResponse({'status': 'error', 'message': 'Server error'}, status=500)
