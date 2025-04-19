from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
# from geopy.distance import geodesic
# from .models import GeoFence, Video
# from .serializers import GeoFenceSerializer
import logging

from django.http import JsonResponse
# from .geofence_utils import is_point_in_geofence

from django.http import JsonResponse
from .BookingService.AcceptBooking import book_tour_service


def book_tour(request):
    """
    Booking API
    """
    return book_tour_service(request)


# def check_geofence(request):
#     """
#     检查用户是否在围栏内
#     """
#     lat = float(request.GET.get('lat'))
#     lng = float(request.GET.get('lng'))
#     geofence_name = request.GET.get('name', 'HKU_Main_Building')
#     in_fence = is_point_in_geofence(geofence_name, lng, lat)
#     return JsonResponse({"inside": in_fence, "geofence": geofence_name})

from django.http import JsonResponse, HttpResponseNotAllowed
from .VerificationService.verification_service import send_verification_code_service





import json
from .VerificationService.VerifyEmailCode import verify_verification_code_service


def send_verification_code(request):
    """
    发送验证码
    """
    if request.method == 'POST':
        email = request.POST.get('email')
        print("view: email", email)
        if not email:
            return JsonResponse({"status": "error", "message": "Email is required"}, status=400)
        result = send_verification_code_service(email)
        return JsonResponse(result)
    else:
        return HttpResponseNotAllowed(['GET'])


def verify_code(request):
    """
    验证验证码接口
    """
    if request.method == 'POST':
        try:
            email = request.POST.get('email')
            code = request.POST.get('code')
            if not email or not code:
                return JsonResponse({"status": "error", "message": "Email and code are required"}, status=400)
            result = verify_verification_code_service(email, code)
            return JsonResponse(result)
        except Exception as e:
            return JsonResponse({"status": "error", "message": str(e)}, status=500)
    return JsonResponse({"status": "error", "message": "Invalid method"}, status=405)
