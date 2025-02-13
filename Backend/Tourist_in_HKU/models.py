from django.db import models

class Appointment(models.Model):
    appointment_id = models.AutoField(primary_key=True)
    appointment_time = models.DateTimeField()

    def __str__(self):
        return f"Appointment ID: {self.appointment_id}, Time: {self.appointment_time}"

