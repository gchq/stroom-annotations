import fetch from 'isomorphic-fetch'

export const REQUEST_FETCH_ANNOTATION_HISTORY = 'REQUEST_FETCH_ANNOTATION_HISTORY';

export const requestFetchAnnotationHistory = (id) => ({
    type: REQUEST_FETCH_ANNOTATION_HISTORY,
    id
})

export const RECEIVE_FETCH_ANNOTATION_HISTORY = 'RECEIVE_FETCH_ANNOTATION_HISTORY';

export const receiveFetchAnnotationHistory = (id, json) => ({
    type: RECEIVE_FETCH_ANNOTATION_HISTORY,
    id,
    history: json,
    receivedAt: Date.now()
})

export const RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED = 'RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED';

export const receiveFetchAnnotationHistoryFailed = (message) => ({
    type: RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED,
    message,
    receivedAt: Date.now()
})

export const fetchAnnotationHistory = (id) => {
    return function(dispatch) {
        dispatch(requestFetchAnnotationHistory(id));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/single/${id}/history`)
              .then(
                response => response.json(),
                // Do not use catch, because that will also catch
                // any errors in the dispatch and resulting render,
                // causing an loop of 'Unexpected batch number' errors.
                // https://github.com/facebook/react/issues/6895
                error => {
                    dispatch(receiveFetchAnnotationHistoryFailed('An error occured.', error))
                    return undefined;
                }
              )
              .then(json => {
                if (json) {
                    dispatch(receiveFetchAnnotationHistory(id, json))
                }
              })
    }
}