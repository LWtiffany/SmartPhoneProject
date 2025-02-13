from django.contrib import admin

# Register your models here.
from django.contrib import admin
from .models import Appointment  # 假设你有一个 Appointment 模型

# 注册模型到 Django Admin
admin.site.register(Appointment)
