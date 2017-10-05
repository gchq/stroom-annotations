import {
    REQUEST_SEARCH_ANNOTATIONS,
    REQUEST_MORE_ANNOTATIONS,
    RECEIVE_SEARCH_ANNOTATIONS,
    RECEIVE_SEARCH_ANNOTATIONS_FAILED
} from '../actions/searchAnnotations'

import {
    RECEIVE_CREATE_ANNOTATION
} from '../actions/createAnnotation'

import {
    RECEIVE_REMOVE_ANNOTATION
} from '../actions/removeAnnotation'

const defaultState = {
    isFetching: false,
    searchTerm: '',
    seekId: undefined,
    seekLastUpdated: undefined,
    annotations: []
}

const manageAnnotations = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case RECEIVE_REMOVE_ANNOTATION: {
            let annotations = state.annotations.filter(a => a.id !== action.id)
            return Object.assign({}, state, {
                    isFetching: false,
                    lastUpdated: action.receivedAt,
                    annotations
                })
        }
        case RECEIVE_CREATE_ANNOTATION: {
            return Object.assign({}, state, {
                isFetching: false,
                lastUpdated: action.receivedAt,
                annotations: [
                    ...state.annotations,
                    action.annotation
                ]
            })
        }
        case REQUEST_SEARCH_ANNOTATIONS: {
            return Object.assign({}, state, {
                isFetching: true,
                searchTerm: action.searchTerm,
                seekId: undefined,
                seekLastUpdated: undefined,
                annotations: []
            })
        }
        case REQUEST_MORE_ANNOTATIONS: {
            return Object.assign({}, state, {
                    isFetching: true
                  })
        }
        case RECEIVE_SEARCH_ANNOTATIONS: {
            let seekId = undefined
            let seekLastUpdated = undefined

            if (action.annotations.length > 0) {
                seekId = action.annotations[action.annotations.length - 1].id
                seekLastUpdated = action.annotations[action.annotations.length - 1].lastUpdated
            }

            return Object.assign({}, state, {
                isFetching: false,
                annotations: [
                    ...state.annotations,
                    ...action.annotations
                ],
                seekId,
                seekLastUpdated,
                lastUpdated: action.receivedAt
            })
        }
        case RECEIVE_SEARCH_ANNOTATIONS_FAILED: {
            return Object.assign({}, state, {
                    isFetching: false,
                    annotations: [],
                    lastUpdated: action.receivedAt,
                    seekId: undefined,
                    seekLastUpdated: undefined
                  })
        }
        default:
            return state;
    }

}

export default manageAnnotations