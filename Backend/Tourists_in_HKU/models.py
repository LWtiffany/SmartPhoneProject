from django.db import models


class Booking(models.Model):
    TIME_SLOTS = [
        ('A', '上午'),
        ('B', '中午'),
        ('C', '下午'),
    ]
    name = models.CharField(max_length=50)
    email = models.EmailField()
    date = models.DateField()
    time_slot = models.CharField(max_length=1, choices=TIME_SLOTS)
    guide = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.name} ({self.email}) @ {self.date} {self.time_slot}"


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
