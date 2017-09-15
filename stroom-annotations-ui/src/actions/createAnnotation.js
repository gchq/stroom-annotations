import fetch from 'isomorphic-fetch'

export const REQUEST_CREATE_ANNOTATION = 'REQUEST_CREATE_ANNOTATION'

export const requestCreateAnnotation = (id) => ({
    type: REQUEST_CREATE_ANNOTATION,
    id
})

export const RECEIVE_CREATE_ANNOTATION = 'RECEIVE_CREATE_ANNOTATION'

export const receiveCreateAnnotation = (id, annotation) => ({
    type: RECEIVE_CREATE_ANNOTATION,
    id,
    annotation
})

export const RECEIVE_CREATE_ANNOTATION_FAILED = 'RECEIVE_CREATE_ANNOTATION_FAILED'

export const receiveCreateAnnotationFailed = (errorMsg) => ({
    type: RECEIVE_CREATE_ANNOTATION_FAILED,
    errorMsg,
    receivedAt: Date.now()
})

export const createAnnotation = (id) => {
    return function(dispatch) {
        dispatch(requestCreateAnnotation(id));

        return fetch(`http://192.168.1.10:8199/annotations/v1/${id}`,
            {
                method: "POST"
            }
        )
              .then(
                response => response.json(),
                // Do not use catch, because that will also catch
                // any errors in the dispatch and resulting render,
                // causing an loop of 'Unexpected batch number' errors.
                // https://github.com/facebook/react/issues/6895
                error => console.log('An error occured.', error)
              )
              .then(json => {
                if (json.id) {
                    dispatch(receiveCreateAnnotation(id, json))
                } else {
                    dispatch(receiveCreateAnnotationFailed(json.msg))
                }
              })
    }
}
