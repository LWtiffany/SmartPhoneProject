from django.urls import path
from . import views

urlpatterns = [
    path('book/', views.book_tour, name='book_tour'),
    path('send_code/', views.send_verification_code, name='send_verification_code'),
    path('verify_code/', views.verify_code, name='verify_code'),
    # path('get_guidance/', views.check_geofence, name='GetGuidance'),
]
