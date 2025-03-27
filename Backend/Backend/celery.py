from __future__ import absolute_import, unicode_literals
import os
from celery import Celery

# 设置环境变量
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'Backend.settings')

# 创建 Celery 实例
app = Celery('Backend')

# 从 Django 设置中加载配置
app.config_from_object('django.conf:settings', namespace='CELERY')

# 自动发现任务
app.autodiscover_tasks(['Tourists_in_HKU.VerificationService'])

