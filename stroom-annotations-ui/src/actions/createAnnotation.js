import fetch from 'isomorphic-fetch'

import { fetchAnnotationHistory } from './fetchAnnotationHistory'

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

export const receiveCreateAnnotationFailed = (message) => ({
    type: RECEIVE_CREATE_ANNOTATION_FAILED,
    message,
    receivedAt: Date.now()
})

export const createAnnotation = (id) => {
    return function(dispatch) {
        dispatch(requestCreateAnnotation(id));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/single/${id}`, {method: "POST"})
            .then(
                response => {
                    if (!response.ok) {
                        throw new Error(response.statusText)
                    }
                    return response.json()
                }
            )
            .then(json => {
                if (json.id) {
                    dispatch(receiveCreateAnnotation(id, json))
                    dispatch(fetchAnnotationHistory(id))
                }
            }).catch(error => {
                dispatch(receiveCreateAnnotationFailed(error.message))
            })
    }
}
