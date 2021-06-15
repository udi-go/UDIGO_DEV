from django.db import models
from user.models import User


class TourPlace(models.Model):
    id = models.BigIntegerField(primary_key=True)
    address = models.CharField(max_length=300, null=True)  # add1 + add2
    areacode = models.PositiveSmallIntegerField()
    cat1 = models.CharField(max_length=50)
    cat2 = models.CharField(max_length=50)
    cat3 = models.CharField(max_length=50)
    content_type_id = models.PositiveSmallIntegerField()
    createdtime = models.CharField(max_length=100)
    image1 = models.TextField(null=True)  # firstimage
    image2 = models.TextField(null=True)  # firstimage2
    mapx = models.DecimalField(max_digits=9, decimal_places=6)
    mapy = models.DecimalField(max_digits=9, decimal_places=6)
    modifiedtime = models.CharField(max_length=100)
    sigungucode = models.PositiveSmallIntegerField()
    tel = models.CharField(max_length=100, null=True)
    title = models.CharField(max_length=200)
    overview = models.TextField(null=True)
    zipcode = models.IntegerField(null=True)
    homepage = models.TextField(null=True)

    user_like = models.ManyToManyField(User, through='UserLikeTourPlace', related_name='user_like_tour_places_set')

    class Meta:
        db_table = 'tour_places'


class KakaoPlace(models.Model):
    id = models.BigIntegerField(primary_key=True)
    title = models.CharField(max_length=200)      # place_name
    place_url = models.CharField(max_length=500)
    category_name = models.CharField(max_length=300)
    category_group_code = models.CharField(max_length=100)
    category_group_name = models.CharField(max_length=100)
    tel = models.CharField(max_length=100)           # phone
    address = models.CharField(max_length=300)       # address_name
    road_address = models.CharField(max_length=300)  # road_address_name
    mapx = models.DecimalField(max_digits=9, decimal_places=6)  # x
    mapy = models.DecimalField(max_digits=9, decimal_places=6)  # y

    user_like = models.ManyToManyField(User, through='UserLikeKakaoPlace', related_name='user_like_kakao_places_set')

    class Meta:
        db_table = 'kakao_places'


class Review(models.Model):
    place_type = models.CharField(max_length=50)  # enum field / kakao & tour
    place_tour = models.ForeignKey(TourPlace, null=True, on_delete=models.CASCADE)
    place_kakao = models.ForeignKey(KakaoPlace, null=True, on_delete=models.CASCADE)
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    grade = models.PositiveSmallIntegerField()
    text = models.TextField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'reviews'


class UserLikeTourPlace(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    place = models.ForeignKey(TourPlace, on_delete=models.CASCADE)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'user_like_tour_places'


class UserLikeKakaoPlace(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    place = models.ForeignKey(KakaoPlace, on_delete=models.CASCADE)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'user_like_kakao_places'