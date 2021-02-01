import numpy as np
import json
import tensorflow as tf
from sklearn.cluster import KMeans
import cv2
import os, glob, shutil


def run_clustering():
    # پاک کردن فرش های خوشه بندی شده ی قبلی
    cmd = 'rm -rf output*'
    os.system(cmd)


    # درست کردن فولدر خورجی
    cmd = 'mkdir output'
    os.system(cmd)

#  تعیین میکنیم از چه ادرسی اطلاعات فرش ها خوانده شود
    input_dir = 'carpet'
    glob_dir = input_dir + '/*.jpg'

# ما اندازه تصاویر را به 224x224 
# تغییر می دهیم تا با اندازه لایه ورودی مدل خود برای استخراج ویژگی مطابقت داشته باشد.
    images = [cv2.resize(cv2.imread(file),
                         (224, 224)) for file in glob.glob(glob_dir)]
    paths = [file for file in glob.glob(glob_dir)]
    images = np.array(np.float32(images).reshape(len(images), -1) / 255)
# اکنون که ویژگی ها را استخراج کردیم ، اکنون می توان خوشه بندی را با استفاده از 
# KMeans انجام دهیم.
    model = tf.keras.applications.MobileNetV2(include_top=False,
                                              weights='imagenet',
                                              input_shape=(224, 224, 3))

    predictions = model.predict(images.reshape(-1, 224, 224, 3))
    pred_images = predictions.reshape(images.shape[0], -1)


    k = 19
    kmodel = KMeans(n_clusters=k, n_jobs=-1, random_state=728)
    kmodel.fit(pred_images)
    kpredictions = kmodel.predict(pred_images)
    shutil.rmtree('output')
# برای اینکه کاربر مبایل مجبور نباشد کل دیتای خوشه بندی شده را دانلود کند ما یک فایل 
# json
# تهیه میکنیم تا اطلاعات رو به صورت متنی هم بتونیم در اختیار کاربر قرار دهیم
    json_response = {}
    for i in range(k):
        os.makedirs(f"output\cluster{i}")
        json_response[f'cluster{i}'] = []
    for i in range(len(paths)):
        shutil.copy2(paths[i], f"output\cluster{kpredictions[i]}")
        json_response[f'cluster{kpredictions[i]}'].append(paths[i])

    with open("json_resp.json", 'w', encoding='utf-8') as f:
        f.write(json.dumps(json_response))

    # now clustering is done and we compress all data and provide that with api
    # پس از انجام خوشه بندی دیتا رو فشرده میکنیم تا قابل دانلود باشد
    print('zipping output')
    cmd = 'zip -r output.zip output*'
    os.system(cmd)


#    با کد زیر متوانیم بفهمیم چه عددی برای تعداد خوشه ها مناسب است
    # find the optimal group number
    # sil = []
    # kl = []
    # kmax = 10
    #
    #
    # for k in range(2, kmax+1):
    #   kmeans2 = KMeans(n_clusters = k).fit(pred_images)
    #   labels = kmeans2.labels_
    #   sil.append(silhouette_score(pred_images, labels, metric = 'euclidean'))
    #   kl.append(k)
    #
    #
    # plt.plot(kl, sil)
    # plt.ylabel('Silhoutte Score')
    # plt.ylabel('K')
    # plt.show()

    # متغیری را که برای این تعریف کرده بودیم که تا خوشه بندی انجام نشده کاربر نتواند دیتا را دانلود کند را 
    # false
    # میکنیم تا یوزر بتواند دیتای خوشه بندی شده را دانلود کند
    from main import set_lock_api
    set_lock_api(loc_api=False)



# def predict_single_image(image_address):
#     my_image = cv2.resize(cv2.imread(image_address), (224, 224))
#     my_image = np.array(np.float32(my_image).reshape(len(my_image), -1) / 255)

