from fastapi import FastAPI, File, UploadFile
import os
import tempfile
from clusterer import run_clustering



app = FastAPI()
storage_path = '/carpet/'
# todo check this 
link_address = '217.69.13.96:8001/output.zip'
lock_api= True
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
        with open(dir_path+storage_path+'/'+file.filename, 'wb') as f:
            # print(dir_path+storage_path+'/'+file.filename)
            for chunk in iter(lambda: file.file.read(10000), b''):
                f.write(chunk)

        # extract content
        # content = textract.process(path)
        # print(content)
        # remove temp file
        run_clustering()
        print('finished running clustering now files have benn moved')
        os.close(_)
        os.remove(path)


@app.get("/get_predictions/")
async def predict_uploaded_image():
    if lock_api:
        return "file not available now"
    return link_address

# if __name__ == "__main__":
    # uvicorn.run(app, host="127.0.0.1", port=8000, log_level="info", reload=True)


