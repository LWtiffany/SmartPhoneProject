from django.urls import path
from . import views

urlpatterns = [
    path('book/', views.book_tour, name='book_tour'),
    # path('success/', views.success, name='success'),
    # path('get_video/', views.get_video, name='get_video'),
]