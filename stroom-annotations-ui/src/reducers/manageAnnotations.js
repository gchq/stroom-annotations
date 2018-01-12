import {
    REQUEST_SEARCH_ANNOTATIONS,
    REQUEST_MORE_ANNOTATIONS,
    RECEIVE_SEARCH_ANNOTATIONS,
    RECEIVE_SEARCH_ANNOTATIONS_FAILED,
    SELECT_ROW
} from '../actions/searchAnnotations'

import {
    RECEIVE_CREATE_ANNOTATION
} from '../actions/createAnnotation'

import {
    RECEIVE_REMOVE_ANNOTATION
} from '../actions/removeAnnotation'

const defaultState = {
    searchTerm: '',
    annotations: [],
    showSearchLoader: false,
    canRequestMore: false
}

const manageAnnotations = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case SELECT_ROW: {
            if (state.selectedAnnotationRowId === action.annotationId) {
                return Object.assign({}, state, {
                    selectedAnnotationRowId: undefined
                })
            } else {
                return Object.assign({}, state, {
                    selectedAnnotationRowId: action.annotationId
                })
            }
        }
        case RECEIVE_REMOVE_ANNOTATION: {
            return Object.assign({}, state, {
                    annotations : state.annotations.filter(a => a.id !== action.id)
                })
        }
        case RECEIVE_CREATE_ANNOTATION: {
            return Object.assign({}, state, {
                annotations: [
                    action.annotation,
                    ...state.annotations
                ]
            })
        }
        case REQUEST_SEARCH_ANNOTATIONS: {
            return Object.assign({}, state, {
                searchTerm: action.searchTerm,
                annotations: [],
                canRequestMore: false,
                showSearchLoader: true
            })
        }
        case REQUEST_MORE_ANNOTATIONS: {
            return Object.assign({}, state, {
                canRequestMore: false,
                showSearchLoader: true
            })
        }
        case RECEIVE_SEARCH_ANNOTATIONS: {
            let annotations = undefined;
            if (action.append) {
                annotations = [
                    ...state.annotations,
                    ...action.annotations,
                ]
            } else {
                annotations = action.annotations
            }

            return Object.assign({}, state, {
                annotations,
                showSearchLoader: false,
                canRequestMore: (action.annotations.length > 0)
            })
        }
        case RECEIVE_SEARCH_ANNOTATIONS_FAILED: {
            return Object.assign({}, state, {
                    annotations: [],
                    showSearchLoader: false,
                    canRequestMore: false
                  })
        }
        default:
            return state;
    }

}

export default (state, action) => manageAnnotations(state, action)