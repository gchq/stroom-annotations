import fetch from 'isomorphic-fetch'

export const REQUEST_UPDATE_ANNOTATION = 'REQUEST_UPDATE_ANNOTATION'

export const requestUpdateAnnotation = (id, content) => ({
    type: REQUEST_UPDATE_ANNOTATION,
    id
})

export const RECEIVE_UPDATE_ANNOTATION = 'RECEIVE_UPDATE_ANNOTATION';
 
 export const receiveUpdateAnnotation = (id, json) => ({
     type: RECEIVE_UPDATE_ANNOTATION,
     id,
     content: json.content,
     receivedAt: Date.now()
 })
 
 export const RECEIVE_UPDATE_ANNOTATION_FAILED = 'RECEIVE_UPDATE_ANNOTATION_FAILED';
 
 export const receiveUpdateAnnotationFailed = (errorMsg) => ({
     type: RECEIVE_UPDATE_ANNOTATION_FAILED,
     errorMsg,
     receivedAt: Date.now()
 })

export const updateAnnotation = (id, content) => {
    return function(dispatch) {
        dispatch(requestUpdateAnnotation(id, content));

        console.log(`Updating Annotation ${id} with content ${content}`)

        return fetch(`http://192.168.1.10:8199/annotations/v1/${id}`,
            {
                method: "PUT",
                body: content
            }
        )
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
                    dispatch(receiveUpdateAnnotation(id, json))
                } else {
                    dispatch(receiveUpdateAnnotationFailed(json.msg))
                }
              })
    }
}
