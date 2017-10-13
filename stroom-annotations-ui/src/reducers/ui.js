import {
    SET_ALLOW_NAVIGATION,
    SET_ANNOTATION_ID
} from '../actions/setupUi'

const defaultState = {
    allowNavigation: true,
    annotationId: undefined
}

const ui = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case SET_ALLOW_NAVIGATION:
            if (!state.allowNavigationAlreadySet) {
                return {
                    ...state,
                    allowNavigation: action.allowNavigation
                }
            } else {
                return state
            }
        case SET_ANNOTATION_ID:
            return {
                ...state,
                annotationId: action.annotationId
            }
        default:
            return state
    }
}

export default ui
