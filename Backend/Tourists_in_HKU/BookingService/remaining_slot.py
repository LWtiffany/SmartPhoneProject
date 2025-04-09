from django.views.decorators.http import require_GET
from django.http import JsonResponse
from Tourists_in_HKU.models import Booking
import logging

logger = logging.getLogger('Tourists_in_HKU')

@require_GET
def check_remaining_slots(request):
    """
    查询剩余预约名额：100 - 当前预约人数
    参数：date=YYYY-MM-DD, time_slot=A/B/C
    """
    date = request.GET.get('date')
    time_slot = request.GET.get('time_slot')

    if not date or not time_slot:
        return JsonResponse({'status': 'error', 'message': 'Missing date or time_slot'}, status=400)

    try:
        count = Booking.objects.filter(date=date, time_slot=time_slot).count()
        remaining = max(0, 100 - count)

        logger.info(f"查询剩余名额：{date} {time_slot} → 已预约 {count}，剩余 {remaining}")

        return JsonResponse({
            'status': 'success',
            'date': date,
            'time_slot': time_slot,
            'booked': count,
            'remaining': remaining
        })

    except Exception as e:
        logger.error(f"查询剩余名额失败：{str(e)}")
        return JsonResponse({'status': 'error', 'message': 'Server error'}, status=500)
