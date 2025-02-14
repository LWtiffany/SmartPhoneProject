from django.shortcuts import render, redirect
from .forms import BookingForm
from .models import Booking

def book_tour(request):
    if request.method == 'POST':
        form = BookingForm(request.POST)
        if form.is_valid():
            form.save()  # 将表单数据保存到数据库
            return redirect('success')  # 重定向到成功页面
    else:
        form = BookingForm()
    return render(request, 'Tourists_in_HKU/book_tour.html', {'form': form})

def success(request):
    return render(request, 'Tourists_in_HKU/success.html')

def get_video(request):
    latitude = request.GET.get('latitude')
    longitude = request.GET.get('longitude')
    video = Video.objects.filter(latitude=latitude, longitude=longitude).first()
    if video:
        return JsonResponse({'video_url': video.video_file.url})
    else:
        return JsonResponse({'error': 'Video not found'}, status=404)

