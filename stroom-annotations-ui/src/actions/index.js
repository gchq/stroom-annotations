export const createAnnotation = (id) => ({
    type: 'CREATE_ANNOTATION',
    id
})

export const updateAnnotation = (content) => ({
    type: 'UPDATE_ANNOTATION',
    content
})

export const removeAnnotation = (id) => ({
    type: 'REMOVE_ANNOTATION',
    id
})