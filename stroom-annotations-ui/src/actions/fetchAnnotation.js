import fetch from 'isomorphic-fetch'

export const REQUEST_FETCH_ANNOTATION = 'REQUEST_FETCH_ANNOTATION';

export const requestFetchAnnotation = (id) => ({
    type: REQUEST_FETCH_ANNOTATION,
    id
})

export const RECEIVE_FETCH_ANNOTATION = 'RECEIVE_FETCH_ANNOTATION';

export const receiveFetchAnnotation = (id, json) => ({
    type: RECEIVE_FETCH_ANNOTATION,
    id,
    annotation: json,
    receivedAt: Date.now()
})

export const RECEIVE_FETCH_ANNOTATION_NOT_EXIST = 'RECEIVE_FETCH_ANNOTATION_NOT_EXIST';

export const receiveFetchAnnotationNotExist = (id) => ({
    type: RECEIVE_FETCH_ANNOTATION_NOT_EXIST,
    id,
    receivedAt: Date.now()
})


export const RECEIVE_FETCH_ANNOTATION_FAILED = 'RECEIVE_FETCH_ANNOTATION_FAILED';

export const receiveFetchAnnotationFailed = (message) => ({
    type: RECEIVE_FETCH_ANNOTATION_FAILED,
    message,
    receivedAt: Date.now()
})

export const fetchAnnotation = (id) => {
    return function(dispatch) {
        dispatch(requestFetchAnnotation(id));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/single/${id}`)
            .then(
                response => {
                    if (response.status === 404) {
                        dispatch(receiveFetchAnnotationNotExist(id))
                    } else if (!response.ok) {
                        throw new Error(response.statusText)
                    }
                    return response.json()
                }
            )
            .then(json => {
                if (json) {
                    dispatch(receiveFetchAnnotation(id, json))
                }
            }).catch(error => {
                dispatch(receiveFetchAnnotationFailed(error))
            })
    }
}
