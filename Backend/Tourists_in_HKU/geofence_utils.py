# import redis
# from shapely.geometry import Point, Polygon
#
# client = redis.Redis(host='localhost', port=6379, db=0)
#
#
# def add_geofence(name, coordinates):
#     for i, (lng, lat) in enumerate(coordinates):
#         client.geoadd(name, (lng, lat, f"{name}_point_{i + 1}"))
#
#
# def get_geofence_points(name):
#     keys = client.keys(f"{name}_point_*")
#     points = client.geopos(name, *keys)
#     return [point for point in points if point]
#
#
# def is_point_in_geofence(name, longitude, latitude):
#     points = get_geofence_points(name)
#     polygon = Polygon(points)
#     point = Point(longitude, latitude)
#     return polygon.contains(point)
