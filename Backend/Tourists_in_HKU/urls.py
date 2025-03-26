from django.urls import path
from . import views

urlpatterns = [
    path('book/', views.book_tour, name='book_tour'),
    # path('get_guidance/', views.check_geofence, name='GetGuidance'),
]