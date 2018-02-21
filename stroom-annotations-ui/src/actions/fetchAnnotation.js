import fetch from 'isomorphic-fetch'

import { sendToSnackbar } from './snackBar'

export const REQUEST_FETCH_ANNOTATION = 'REQUEST_FETCH_ANNOTATION';

export const requestFetchAnnotation = (apiCallId, indexUuid, id) => ({
    type: REQUEST_FETCH_ANNOTATION,
    id,
    apiCallId
})

export const RECEIVE_FETCH_ANNOTATION = 'RECEIVE_FETCH_ANNOTATION';

export const receiveFetchAnnotation = (apiCallId, id, json) => ({
    type: RECEIVE_FETCH_ANNOTATION,
    id,
    annotation: json,
    apiCallId
})

export const RECEIVE_FETCH_ANNOTATION_NOT_EXIST = 'RECEIVE_FETCH_ANNOTATION_NOT_EXIST';

export const receiveFetchAnnotationNotExist = (apiCallId, id) => ({
    type: RECEIVE_FETCH_ANNOTATION_NOT_EXIST,
    id,
    apiCallId
})


export const RECEIVE_FETCH_ANNOTATION_FAILED = 'RECEIVE_FETCH_ANNOTATION_FAILED';

export const receiveFetchAnnotationFailed = (apiCallId, message) => ({
    type: RECEIVE_FETCH_ANNOTATION_FAILED,
    message,
               apiCallId
})

let apiCallId = 0

export const fetchAnnotation = (indexUuid, id) => {
    return function(dispatch, getState) {
        const thisApiCallId = `fetchAnnotation-${apiCallId}`
        apiCallId += 1

        dispatch(requestFetchAnnotation(thisApiCallId, id));

        const state = getState()
        const jwsToken = state.authentication.idToken

        return fetch(`${state.config.annotationsServiceUrl}/annotations/v1/single/${indexUuid}/${id}`, {
            headers: {
                'Accept': 'application/json',
                'Authorization': 'Bearer ' + jwsToken
            },
        })
            .then(
                response => {
                    if (response.status === 404) {
                        dispatch(receiveFetchAnnotationNotExist(thisApiCallId, id))
                    } else if (!response.ok) {
                        throw new Error(response.statusText)
                    }
                    return response.json()
                },
                error => {
                    dispatch(receiveFetchAnnotationFailed(thisApiCallId, error))
                    dispatch(sendToSnackbar('Failed to Fetch Annotations ' + error))
                }
            )
            .then(json => {
                if (json) {
                    dispatch(receiveFetchAnnotation(thisApiCallId, id, json))
                }
            })
    }
}
