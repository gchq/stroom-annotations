import fetch from 'isomorphic-fetch'

import { sendToSnackbar } from './snackBar'

export const REQUEST_CREATE_ANNOTATION = 'REQUEST_CREATE_ANNOTATION'

export const requestCreateAnnotation = (apiCallId, id) => ({
    type: REQUEST_CREATE_ANNOTATION,
    id,
    apiCallId
})

export const RECEIVE_CREATE_ANNOTATION = 'RECEIVE_CREATE_ANNOTATION'

export const receiveCreateAnnotation = (apiCallId, id, annotation) => ({
    type: RECEIVE_CREATE_ANNOTATION,
    id,
    annotation,
    apiCallId
})

export const RECEIVE_CREATE_ANNOTATION_FAILED = 'RECEIVE_CREATE_ANNOTATION_FAILED'

export const receiveCreateAnnotationFailed = (apiCallId, message) => ({
    type: RECEIVE_CREATE_ANNOTATION_FAILED,
    message,
    apiCallId
})

let apiCallId = 0

export const createAnnotation = (indexUuid, id) => {
    return function(dispatch, getState) {
        const thisApiCallId = `createAnnotation-${apiCallId}`
        apiCallId += 1

        dispatch(requestCreateAnnotation(thisApiCallId, id))

        const state = getState()
        const jwsToken = state.authentication.idToken

        return fetch(`${state.config.annotationsServiceUrl}/annotations/v1/single/${indexUuid}/${id}`, {
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + jwsToken
            },
            method: "POST",
            mode: 'cors'
        })
            .then(
                response => {
                    if (!response.ok) {
                        throw new Error(response.statusText)
                    }
                    return response.json()
                },
                error => {
                    dispatch(receiveCreateAnnotationFailed(thisApiCallId, error.message))
                    dispatch(sendToSnackbar('Failed to Create Annotation ' + error.message))
                }
            )
            .then(json => {
                if (json.id) {
                    dispatch(receiveCreateAnnotation(thisApiCallId, id, json))
                    dispatch(sendToSnackbar('Annotation Created'))
                }
            })
    }
}
