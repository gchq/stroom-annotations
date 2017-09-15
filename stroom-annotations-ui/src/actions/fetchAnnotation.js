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

export const RECEIVE_FETCH_ANNOTATION_FAILED = 'RECEIVE_FETCH_ANNOTATION_FAILED';

export const receiveFetchAnnotationFailed = (errorMsg) => ({
    type: RECEIVE_FETCH_ANNOTATION_FAILED,
    errorMsg,
    receivedAt: Date.now()
})

export const fetchAnnotation = (id) => {
    return function(dispatch) {
        dispatch(requestFetchAnnotation(id));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/${id}`)
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
                    dispatch(receiveFetchAnnotation(id, json))
                } else {
                    dispatch(receiveFetchAnnotationFailed(json.msg))
                }
              })
    }
}
