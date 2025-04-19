from django import forms
from .models import Booking

class BookingForm(forms.ModelForm):
    # 预约时段选项
    TIME_SLOTS = [
        ('A', '上午时段（A）'),
        ('B', '中午时段（B）'),
        ('C', '下午时段（C）'),
    ]

    # 时段选择
    time_slot = forms.ChoiceField(choices=TIME_SLOTS, label="预约时段")

    # 用户姓名输入
    name = forms.CharField(label='Your Name', max_length=50, required=True)

    # 是否需要导游，True 或 False
    guide = forms.BooleanField(label="需要导游", required=False)  # 默认为 False，表示不需要导游

    class Meta:
        model = Booking
        fields = ['email', 'date', 'time_slot', 'name', 'guide']  # 增加了 'name' 和 'guide'

