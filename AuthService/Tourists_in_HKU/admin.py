from django.contrib import admin

# Register your models here.
from django.contrib import admin
from .models import Booking

# 注册模型到 Django Admin
admin.site.register(Booking)
