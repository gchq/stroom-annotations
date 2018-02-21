import fetch from 'isomorphic-fetch'

import { sendToSnackbar } from './snackBar'

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

export const fetchAnnotationHistory = (indexUuid, id) => {
    return function(dispatch, getState) {
        const thisApiCallId = `fetchAnnotationHistory-${apiCallId}`
        apiCallId += 1

        dispatch(requestFetchAnnotationHistory(thisApiCallId, id));

        const state = getState()
        const jwsToken = state.authentication.idToken

        return fetch(`${state.config.annotationsServiceUrl}/annotations/v1/single/${indexUuid}/${id}/history`, {
            headers: {
                'Accept': 'application/json',
                'Authorization': 'Bearer ' + jwsToken
            },
        })
              .then(
                response => response.json(),
                // Do not use catch, because that will also catch
                // any errors in the dispatch and resulting render,
                // causing an loop of 'Unexpected batch number' errors.
                // https://github.com/facebook/react/issues/6895
                error => {
                    dispatch(receiveFetchAnnotationHistoryFailed(thisApiCallId, error))
                }
              )
              .then(json => {
                if (json) {
                    dispatch(receiveFetchAnnotationHistory(thisApiCallId, id, json))
                    dispatch(sendToSnackbar('Failed to Fetch Annotation History ' + json))
                }
              })
    }
}