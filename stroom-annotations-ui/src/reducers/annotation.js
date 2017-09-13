const annotation = (state={}, action) => {
    switch(action.type) {
        case 'CREATE_ANNOTATION':
            return {
                id: action.id,
                content: ''
            }

        case 'UPDATE_ANNOTATION':
            return {
                ...state,
                content: action.content
            }

        case 'REMOVE_ANNOTATION':
            return {}

        default:
            return state;
    }

}

export default annotation