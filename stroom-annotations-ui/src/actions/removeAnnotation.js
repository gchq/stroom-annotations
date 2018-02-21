import fetch from 'isomorphic-fetch'

import { sendToSnackbar } from './snackBar'

export const REQUEST_REMOVE_ANNOTATION = 'REQUEST_REMOVE_ANNOTATION'

export const requestRemoveAnnotation = (apiCallId, id) => ({
    type: REQUEST_REMOVE_ANNOTATION,
    id,
    apiCallId
})

export const RECEIVE_REMOVE_ANNOTATION = 'RECEIVE_REMOVE_ANNOTATION'

export const receiveRemoveAnnotation = (apiCallId, id) => ({
    type: RECEIVE_REMOVE_ANNOTATION,
    id,
    apiCallId
})

export const RECEIVE_REMOVE_ANNOTATION_FAILED = 'RECEIVE_REMOVE_ANNOTATION_FAILED'

export const receiveRemoveAnnotationFailed = (apiCallId, message) => ({
    type: RECEIVE_REMOVE_ANNOTATION_FAILED,
    message,
    apiCallId
})

let apiCallId = 0

export const removeAnnotation = (indexUuid, id) => {
    return function(dispatch, getState) {
        const thisApiCallId = `removeAnnotation-${apiCallId}`
        apiCallId += 1

        dispatch(requestRemoveAnnotation(thisApiCallId, id));

        const state = getState()
        const jwsToken = state.authentication.idToken

        return fetch(`${state.config.annotationsServiceUrl}/annotations/v1/single/${indexUuid}/${id}`, {
            headers: {
                'Accept': 'application/json',
                'Authorization': 'Bearer ' + jwsToken
            },
            method: "DELETE",
            mode: 'cors'
        })
              .then(
                response => {
                    dispatch(receiveRemoveAnnotation(thisApiCallId, id))
                    dispatch(sendToSnackbar('Annotation Removed'))
                },
                error => {
                    dispatch(receiveRemoveAnnotationFailed(thisApiCallId, error))
                    dispatch(sendToSnackbar('Failed to Remove Annotation ' + error))
                }
              )
    }
}
