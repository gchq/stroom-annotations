import fetch from 'isomorphic-fetch'

import { sendToSnackbar } from './snackBar'

export const EDIT_ANNOTATION = 'EDIT_ANNOTATION'

export const editAnnotation = (id, updates) => ({
    type: EDIT_ANNOTATION,
    id,
    updates
})

export const REQUEST_UPDATE_ANNOTATION = 'REQUEST_UPDATE_ANNOTATION'

export const requestUpdateAnnotation = (apiCallId, id, annotation) => ({
    type: REQUEST_UPDATE_ANNOTATION,
    id,
    annotation,
    apiCallId
})

export const RECEIVE_UPDATE_ANNOTATION = 'RECEIVE_UPDATE_ANNOTATION';
 
export const receiveUpdateAnnotation = (apiCallId, id, annotation) => ({
    type: RECEIVE_UPDATE_ANNOTATION,
    id,
    annotation,
    apiCallId
})

export const RECEIVE_UPDATE_ANNOTATION_FAILED = 'RECEIVE_UPDATE_ANNOTATION_FAILED';

export const receiveUpdateAnnotationFailed = (apiCallId, message) => ({
    type: RECEIVE_UPDATE_ANNOTATION_FAILED,
    message,
    apiCallId
})

let apiCallId = 0

export const updateAnnotation = (indexUuid, id, annotation) => {
    return function(dispatch, getState) {
        const thisApiCallId = `updateAnnotation-${apiCallId}`
        apiCallId += 1

        dispatch(requestUpdateAnnotation(thisApiCallId, id, annotation));

        const state = getState()
        const jwsToken = state.authentication.idToken

        return fetch(`${state.config.annotationsServiceUrl}/annotations/v1/single/${indexUuid}/${id}`,
            {
                method: "PUT",
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + jwsToken
                },
                mode: 'cors',
                body: JSON.stringify(annotation)
            }
        )
              .then(
                response => response.json(),
                // Do not use catch, because that will also catch
                // any errors in the dispatch and resulting render,
                // causing an loop of 'Unexpected batch number' errors.
                // https://github.com/facebook/react/issues/6895
                error => {
                    dispatch(receiveUpdateAnnotationFailed(thisApiCallId, error))
                    dispatch(sendToSnackbar('Failed to Update Annotation ' + error))
                }
              )
              .then(json => {
                if (json.id) {
                    dispatch(receiveUpdateAnnotation(thisApiCallId, id, json))
                    dispatch(sendToSnackbar('Annotation Updated'))
                }
              })
    }
}
