from django import forms
from .models import Booking


class BookingForm(forms.ModelForm):
    TIME_SLOTS = [
        ('A', '上午时段（A）'),
        ('B', '中午时段（B）'),
        ('C', '下午时段（C）'),
    ]

    time_slot = forms.ChoiceField(choices=TIME_SLOTS, label="预约时段")

    class Meta:
        model = Booking
        fields = ['email', 'date', 'time_slot']
