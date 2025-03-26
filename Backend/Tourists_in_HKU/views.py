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


