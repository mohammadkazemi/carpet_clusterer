from fastapi import FastAPI, File, UploadFile
import json
import os
import tempfile
from fastapi.responses import FileResponse

from clusterer import run_clustering
# from clusterer import predict_single_image

lock_api = True


def set_lock_api(loc_api: bool):
    global lock_api
    lock_api = loc_api


app = FastAPI()

storage_path = '/carpet/'
link_address = 'output.zip'


# uploaded_file_counter = 0

@app.post("/uploadfile/")
async def create_upload_file(file: UploadFile = File(...)):
    parse(file)
    return {"filename": file.filename}


def parse(file: UploadFile = File(...)):
    extension = os.path.splitext(file.filename)[1]
    if extension == '.jpg':
        _, path = tempfile.mkstemp(prefix='parser_', suffix=extension)
        print(_)
        print(path)
        dir_path = os.getcwd()
        with open(dir_path + storage_path + '/' + file.filename, 'wb') as f:
            # print(dir_path+storage_path+'/'+file.filename)
            for chunk in iter(lambda: file.file.read(10000), b''):
                f.write(chunk)

        # extract content
        # content = textract.process(path)
        # print(content)
        # remove temp file
        run_clustering()
        print('finished running clustering')
        os.close(_)
        os.remove(path)


@app.get("/get_predictions/")
async def predict_uploaded_image():
    if lock_api:
        return "file is not available for now"
    else:
        set_lock_api(loc_api=True)
        return FileResponse(link_address)


@app.get("/get_predictions_clusters/")
async def get_predictions_clusters_result():
    if lock_api:
        return "file is not available for now"
    else:
        set_lock_api(loc_api=True)
        with open("json_resp.json", 'r', encoding='utf-8') as f:
            return json.loads(f.read())


# @app.post("/predict_with_saved_model/")
# async def predict_with_saved_model(file: UploadFile = File(...)):
#     single_image = await file.read()
#     predict_single_image(single_image)


# if __name__ == "__main__":
#     uvicorn.run(app, host="127.0.0.1", port=8000, log_level="info", reload=True)
