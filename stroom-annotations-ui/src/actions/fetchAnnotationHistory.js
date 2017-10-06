import fetch from 'isomorphic-fetch'

export const REQUEST_FETCH_ANNOTATION_HISTORY = 'REQUEST_FETCH_ANNOTATION_HISTORY';

export const requestFetchAnnotationHistory = (apiCallId, id) => ({
    type: REQUEST_FETCH_ANNOTATION_HISTORY,
    id,
    apiCallId
})

export const RECEIVE_FETCH_ANNOTATION_HISTORY = 'RECEIVE_FETCH_ANNOTATION_HISTORY';

export const receiveFetchAnnotationHistory = (apiCallId, id, json) => ({
    type: RECEIVE_FETCH_ANNOTATION_HISTORY,
    id,
    history: json,
    apiCallId
})

export const RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED = 'RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED';

export const receiveFetchAnnotationHistoryFailed = (apiCallId, message) => ({
    type: RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED,
    message,
    apiCallId
})

let apiCallId = 0

export const fetchAnnotationHistory = (id) => {
    return function(dispatch) {
        const thisApiCallId = `fetchAnnotationHistory-${apiCallId}`
        apiCallId += 1

        dispatch(requestFetchAnnotationHistory(thisApiCallId, id));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/single/${id}/history`)
              .then(
                response => response.json(),
                // Do not use catch, because that will also catch
                // any errors in the dispatch and resulting render,
                // causing an loop of 'Unexpected batch number' errors.
                // https://github.com/facebook/react/issues/6895
                error => {
                    dispatch(receiveFetchAnnotationHistoryFailed(thisApiCallId, error))
                    return undefined;
                }
              )
              .then(json => {
                if (json) {
                    dispatch(receiveFetchAnnotationHistory(thisApiCallId, id, json))
                }
              })
    }
}