from django.db import models


class User(models.Model):
    SOCIALCHOICE = (
        (0, 'kakao'),
        (1, 'naver'),
        (2, 'google')
    )
    social_type = models.IntegerField(choices=SOCIALCHOICE, default=0)
    kakao_id = models.IntegerField(null=True)
    naver_id = models.IntegerField(null=True)
    google_id = models.IntegerField(null=True)
    nickname = models.CharField(max_length=25)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'users'