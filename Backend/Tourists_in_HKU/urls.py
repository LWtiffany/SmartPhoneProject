from django.urls import path
from . import views
from .BookingService.remaining_slot import check_remaining_slots
from .BookingService.get_information import get_bookings_by_email
urlpatterns = [
    path('book/', views.book_tour, name='book_tour'),
    path('send_code/', views.send_verification_code, name='send_verification_code'),
    path('verify_code/', views.verify_code, name='verify_code'),
    path('remaining_slots/',check_remaining_slots, name='check_remaining_slots'),
    path('get_information/',get_bookings_by_email, name='get_booking_information'),
    # path('get_guidance/', views.check_geofence, name='GetGuidance'),
]
