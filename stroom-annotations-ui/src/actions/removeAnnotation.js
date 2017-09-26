import fetch from 'isomorphic-fetch'

export const REQUEST_REMOVE_ANNOTATION = 'REQUEST_REMOVE_ANNOTATION'

export const requestRemoveAnnotation = (id) => ({
    type: REQUEST_REMOVE_ANNOTATION,
    id
})

export const RECEIVE_REMOVE_ANNOTATION = 'RECEIVE_REMOVE_ANNOTATION'

export const receiveRemoveAnnotation = (id) => ({
    type: RECEIVE_REMOVE_ANNOTATION,
    id
})

export const RECEIVE_REMOVE_ANNOTATION_FAILED = 'RECEIVE_REMOVE_ANNOTATION_FAILED'

export const receiveRemoveAnnotationFailed = (errorMsg) => ({
    type: RECEIVE_REMOVE_ANNOTATION_FAILED,
    errorMsg,
    receivedAt: Date.now()
})

export const removeAnnotation = (id) => {
    return function(dispatch) {
        dispatch(requestRemoveAnnotation(id));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/single/${id}`,
            {
                method: "DELETE"
            }
        )
              .then(
                response => dispatch(receiveRemoveAnnotation(id)),
                error => dispatch(receiveRemoveAnnotationFailed(error))
              )
    }
}
