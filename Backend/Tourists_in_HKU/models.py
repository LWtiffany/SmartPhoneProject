from django.db import models

class Booking(models.Model):
    name = models.CharField(max_length=100)  # 用户姓名
    email = models.EmailField()  # 用户邮箱
    phone = models.CharField(max_length=15)  # 用户电话
    date = models.DateField()  # 预约日期
    time = models.TimeField()  # 预约时间
    notes = models.TextField(blank=True, null=True)  # 备注（可选）

    def __str__(self):
        return f"{self.name} - {self.date} {self.time}"
class GeoFence(models.Model):
    name = models.CharField(max_length=100)
    latitude = models.FloatField()
    longitude = models.FloatField()
    radius = models.FloatField()  # 单位：米

    def __str__(self):
        return f"{self.name} - ({self.latitude}, {self.longitude})"

class Video(models.Model):
    title = models.CharField(max_length=100)
    latitude = models.FloatField()
    longitude = models.FloatField()
    video_file = models.FileField(upload_to='videos/')

    def __str__(self):
        return self.title
